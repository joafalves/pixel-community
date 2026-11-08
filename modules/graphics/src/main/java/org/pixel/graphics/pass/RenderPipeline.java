package org.pixel.graphics.pass;

import org.pixel.commons.lifecycle.Disposable;
import org.pixel.graphics.backend.PassState;
import org.pixel.graphics.backend.RenderBackend;
import org.pixel.graphics.backend.RenderData;
import org.pixel.graphics.canvas.Canvas;
import org.pixel.graphics.draw.Batcher;
import org.pixel.graphics.draw.DrawList;
import org.pixel.graphics.draw.Layer;
import org.pixel.math.Matrix4;

import java.util.ArrayList;
import java.util.List;

/**
 * An ordered list of render passes over layers, built once and rendered every frame:
 * <pre>
 * pipeline = RenderPipeline.builder(graphics().getRenderBackend())
 *         .pass(camera, WORLD, FX)        // world layers, through the camera
 *         .pass(screen, HUD)              // HUD in screen space
 *         .build();
 * Canvas world = pipeline.canvas(WORLD);  // record during draw(), then:
 * pipeline.render();                      // sort, cull, batch and submit; layers are cleared for the next frame
 * </pre>
 * Each frame, every layer is sorted once (sorted layers) and batched once per pass that draws it, with commands
 * outside that pass's view culled. A layer can be drawn by several passes (e.g. the world again through a minimap
 * view). Layers in a pass are drawn in the listed order, each with its own blend mode.
 * <p>
 * No GL here: the pipeline talks to a {@link RenderBackend}. Allocation-free per frame once warmed up. Not
 * thread-safe; record and render on the render thread.
 */
public final class RenderPipeline implements Disposable {

    /**
     * A pass with direct backend access (drawing something the pipeline doesn't know about). It must leave the
     * backend outside a pass ({@code beginPass} matched by {@code endPass}), even when it fails.
     */
    @FunctionalInterface
    public interface CustomPass {
        void render(RenderBackend backend);
    }

    private final RenderBackend backend;
    private final LayerState[] layers;
    private final Pass[] passes;
    private final Batcher batcher = new Batcher();
    private final RenderData renderData;
    private final PassState passState = new PassState();
    private final Matrix4 inverse = new Matrix4();
    private final float[] bounds = new float[4];
    private int frame;
    private boolean rendering;
    private boolean disposed;

    private RenderPipeline(RenderBackend backend, List<LayerState> layers, List<Pass> passes) {
        this.backend = backend;
        this.layers = layers.toArray(new LayerState[0]);
        this.passes = passes.toArray(new Pass[0]);
        this.renderData = new RenderData(backend.capabilities().maxTextureSlots());
    }

    /**
     * Start building a pipeline.
     *
     * @param backend The render backend ({@code graphics().getRenderBackend()}).
     */
    public static Builder builder(RenderBackend backend) {
        if (backend == null) {
            throw new IllegalArgumentException("Render backend cannot be null.");
        }
        return new Builder(backend);
    }

    /**
     * The canvas recording into a layer. Always the same object for a layer; its transform and clip are reset after
     * every {@link #render()}. Record before {@code render()}, not from inside it (e.g. from a custom pass).
     *
     * @throws IllegalArgumentException If no pass of this pipeline draws the layer.
     * @throws IllegalStateException    While rendering, or after {@link #dispose()}.
     */
    public Canvas canvas(Layer layer) {
        if (disposed) {
            throw new IllegalStateException("Render pipeline has been disposed.");
        }
        if (rendering) {
            throw new IllegalStateException("Record into layers before render(), not while rendering.");
        }
        return state(layer).canvas;
    }

    /**
     * Render every pass in order, then clear the layers for the next frame.
     */
    public void render() {
        if (disposed) {
            throw new IllegalStateException("Render pipeline has been disposed.");
        }
        if (rendering) {
            throw new IllegalStateException("render() called from inside a render pass.");
        }
        rendering = true;
        frame++;
        try {
            for (LayerState layer : layers) {
                layer.canvas.flush();
            }
            for (Pass pass : passes) {
                if (pass.custom != null) {
                    pass.custom.render(backend);
                } else {
                    renderLayers(pass);
                }
            }
        } finally {
            for (LayerState layer : layers) {
                layer.list.clear();
                layer.canvas.reset();
            }
            rendering = false;
        }
    }

    private void renderLayers(Pass pass) {
        float[] cull = cullBounds(pass.view.getViewMatrix());
        for (LayerState layer : pass.layers) {
            DrawList list = layer.list;
            if (list.size() == 0) {
                continue;
            }
            if (layer.preparedFrame != frame) { // sort once per frame, even when several passes draw the layer
                layer.canvas.flush(); // anything recorded since the start of render() (e.g. by a custom pass)
                list.prepare();
                layer.preparedFrame = frame;
            }
            batcher.build(list, backend.capabilities(), renderData, cull);
            if (renderData.batches().size() == 0) {
                continue; // everything culled
            }
            passState.set(pass.view.getViewMatrix(), layer.layer.getBlendMode());
            backend.beginPass(passState);
            try {
                backend.submit(renderData);
            } finally {
                backend.endPass();
            }
        }
    }

    /**
     * The view's visible area in layer coordinates: the clip-space square through the inverse view matrix.
     *
     * @return {@code bounds}, or {@code null} (no culling) if the matrix can't be inverted.
     */
    private float[] cullBounds(Matrix4 view) {
        float[][] src = view.toUnsafeArray();
        float[][] dst = inverse.toUnsafeArray();
        for (int i = 0; i < 4; i++) {
            System.arraycopy(src[i], 0, dst[i], 0, 4);
        }
        inverse.invert();
        float[][] m = dst;
        float x0 = Float.POSITIVE_INFINITY, y0 = Float.POSITIVE_INFINITY;
        float x1 = Float.NEGATIVE_INFINITY, y1 = Float.NEGATIVE_INFINITY;
        for (int corner = 0; corner < 4; corner++) {
            float nx = (corner & 1) == 0 ? -1f : 1f, ny = (corner & 2) == 0 ? -1f : 1f;
            float x = m[0][0] * nx + m[1][0] * ny + m[3][0];
            float y = m[0][1] * nx + m[1][1] * ny + m[3][1];
            x0 = Math.min(x0, x);
            x1 = Math.max(x1, x);
            y0 = Math.min(y0, y);
            y1 = Math.max(y1, y);
        }
        if (!Float.isFinite(x0) || !Float.isFinite(y0) || !Float.isFinite(x1) || !Float.isFinite(y1)) {
            return null;
        }
        bounds[0] = x0;
        bounds[1] = y0;
        bounds[2] = x1;
        bounds[3] = y1;
        return bounds;
    }

    private LayerState state(Layer layer) {
        for (LayerState state : layers) {
            if (state.layer == layer) {
                return state;
            }
        }
        throw new IllegalArgumentException("No pass of this pipeline draws " + layer + ".");
    }

    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        for (LayerState layer : layers) {
            layer.list.clear();
            layer.canvas.reset();
        }
    }

    // region model

    private static final class LayerState {
        final Layer layer;
        final DrawList list;
        final Canvas canvas;
        final List<View> views = new ArrayList<>(); // every view that draws the layer (for anti-aliasing scales)
        int preparedFrame;

        LayerState(Layer layer) {
            this.layer = layer;
            this.list = new DrawList(layer.getMode());
            this.canvas = new Canvas(list);
        }
    }

    private static final class Pass {
        final View view;
        final LayerState[] layers;
        final CustomPass custom;

        Pass(View view, LayerState[] layers, CustomPass custom) {
            this.view = view;
            this.layers = layers;
            this.custom = custom;
        }
    }

    /**
     * Builds a {@link RenderPipeline}; passes run in the order they are added.
     */
    public static final class Builder {
        private final RenderBackend backend;
        private final List<LayerState> layers = new ArrayList<>();
        private final List<Pass> passes = new ArrayList<>();

        private Builder(RenderBackend backend) {
            this.backend = backend;
        }

        /**
         * Draw layers through a view, in the given order.
         *
         * @param view   The view (e.g. a camera, or screen space for a HUD).
         * @param layers The layers (at least one; a layer may also appear in other passes).
         */
        public Builder pass(View view, Layer... layers) {
            if (view == null || layers == null || layers.length == 0) {
                throw new IllegalArgumentException("A pass needs a view and at least one layer.");
            }
            LayerState[] states = new LayerState[layers.length];
            for (int i = 0; i < layers.length; i++) {
                if (layers[i] == null) {
                    throw new IllegalArgumentException("Layers cannot be null.");
                }
                for (int j = 0; j < i; j++) {
                    if (layers[j] == layers[i]) {
                        throw new IllegalArgumentException(layers[i] + " is listed twice in one pass.");
                    }
                }
                states[i] = stateFor(layers[i], view);
            }
            passes.add(new Pass(view, states, null));
            return this;
        }

        /**
         * Run custom code with direct backend access at this point of the frame.
         */
        public Builder custom(CustomPass pass) {
            if (pass == null) {
                throw new IllegalArgumentException("Custom pass cannot be null.");
            }
            passes.add(new Pass(null, null, pass));
            return this;
        }

        public RenderPipeline build() {
            if (passes.isEmpty()) {
                throw new IllegalStateException("A pipeline needs at least one pass.");
            }
            for (LayerState state : layers) {
                state.canvas.setViews(state.views.toArray(new View[0]));
            }
            return new RenderPipeline(backend, layers, passes);
        }

        private LayerState stateFor(Layer layer, View view) {
            LayerState state = null;
            for (LayerState existing : layers) {
                if (existing.layer == layer) {
                    state = existing; // drawn by an earlier pass too: the same commands
                }
            }
            if (state == null) {
                state = new LayerState(layer);
                layers.add(state);
            }
            if (!state.views.contains(view)) {
                state.views.add(view);
            }
            return state;
        }
    }

    // endregion
}
