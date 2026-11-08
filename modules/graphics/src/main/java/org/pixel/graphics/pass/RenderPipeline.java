package org.pixel.graphics.pass;

import org.pixel.commons.lifecycle.Disposable;
import org.pixel.content.Texture;
import org.pixel.graphics.backend.PassState;
import org.pixel.graphics.backend.RenderBackend;
import org.pixel.graphics.backend.RenderData;
import org.pixel.graphics.backend.BatchList;
import org.pixel.graphics.backend.RenderTarget;
import org.pixel.graphics.canvas.Canvas;
import org.pixel.graphics.draw.Batcher;
import org.pixel.graphics.draw.DrawList;
import org.pixel.graphics.draw.Layer;
import org.pixel.graphics.render.BlendMode;
import org.pixel.math.Matrix4;

import java.util.ArrayList;
import java.util.List;

/**
 * An ordered list of render passes, built once and rendered every frame:
 * <pre>
 * RenderPipeline.Builder builder = RenderPipeline.builder(graphics().getRenderBackend());
 * RenderTarget scene = builder.screenTarget(1f);   // follows the window size
 * pipeline = builder
 *         .pass(camera, scene, WORLD, FX)          // world layers into the scene target
 *         .post(scene, bloom, vignette)            // effects, then to the screen
 *         .pass(screen, HUD)                       // HUD on top, unaffected by the effects
 *         .build();
 * Canvas world = pipeline.canvas(WORLD);           // record during draw(), then:
 * pipeline.render();                               // layers are cleared for the next frame
 * </pre>
 * <ul>
 *   <li><b>Layer passes</b> draw layers through a view, to the screen or into a target. Each layer is sorted once per
 *   frame and batched once per pass that draws it, with commands outside the view culled; layers keep their own
 *   blend mode. A layer can be drawn by several passes (e.g. the world again into a minimap target).</li>
 *   <li><b>Targets</b> are cleared to their clear color the first time a pass draws into them in a frame. Targets
 *   created by the builder ({@link Builder#screenTarget}, {@link Builder#target}) belong to the pipeline; targets
 *   created with the backend belong to the caller.</li>
 *   <li><b>Post passes</b> run effects in a chain from a target to the screen or another target. To the screen, the
 *   result composites over it, so whatever the target leaves transparent shows the screen behind it; into a
 *   target, it replaces the target's contents.</li>
 *   <li><b>Custom passes</b> get direct backend access. Targets they draw into are not tracked: a later layer pass
 *   still clears them first if nothing else drew into them this frame.</li>
 * </ul>
 * {@link #lastFrame()} tells what the last frame did (statistics per pass and layer, batches and why they broke) and
 * finds pickable commands ({@code .pick(id)}) at a point. Effects passed to the builder are disposed with the
 * pipeline. No GL here: the pipeline talks to a
 * {@link RenderBackend}. Allocation-free per frame once warmed up. Not thread-safe; record and render on the render
 * thread.
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
    private final TargetState[] targets;
    private final Pass[] passes;
    private final PostEffect[] effects;
    private final Batcher batcher = new Batcher();
    private final RenderData renderData;
    private final PassState passState = new PassState();
    private final Matrix4 inverse = new Matrix4();
    private final float[] bounds = new float[4];
    private final ShaderEffect copy = new ShaderEffect("vec4 effect(vec2 uv) {\n    return texture(uSource, uv);\n}\n");
    private final FrameInfo frameInfo;
    private final int[] screen; // screen size, updated by render()
    private int frame;
    private boolean rendering;
    private boolean disposed;

    private RenderPipeline(Builder builder) {
        this.backend = builder.backend;
        this.layers = builder.layers.toArray(new LayerState[0]);
        this.targets = builder.targets.toArray(new TargetState[0]);
        this.passes = builder.passes.toArray(new Pass[0]);
        this.effects = builder.effects.toArray(new PostEffect[0]);
        this.renderData = new RenderData(backend.capabilities().maxTextureSlots());
        this.screen = builder.screen;

        List<FrameInfo.PassStats> passStats = new ArrayList<>();
        List<FrameInfo.LayerStats> layerStats = new ArrayList<>();
        for (int p = 0; p < passes.length; p++) {
            Pass pass = passes[p];
            FrameInfo.LayerStats[] stats = new FrameInfo.LayerStats[pass.kind == Kind.LAYERS ? pass.layers.length : 0];
            for (int i = 0; i < stats.length; i++) {
                stats[i] = new FrameInfo.LayerStats(pass.layers[i].layer, p);
                layerStats.add(stats[i]);
            }
            pass.layerStats = stats;
            FrameInfo.PassKind kind = switch (pass.kind) {
                case LAYERS -> FrameInfo.PassKind.LAYERS;
                case POST -> FrameInfo.PassKind.POST;
                default -> FrameInfo.PassKind.CUSTOM;
            };
            pass.stats = new FrameInfo.PassStats(p, kind, pass.target != null ? pass.target.target : null, stats);
            passStats.add(pass.stats);
        }
        Layer[] pickLayers = new Layer[layers.length];
        for (int i = 0; i < layers.length; i++) {
            pickLayers[i] = layers[i].layer;
        }
        this.frameInfo = new FrameInfo(passStats.toArray(new FrameInfo.PassStats[0]),
                layerStats.toArray(new FrameInfo.LayerStats[0]), pickLayers);
    }

    /**
     * What the last {@link #render()} did, and its pickable commands. The same object every time, updated in place
     * by each {@code render()}.
     */
    public FrameInfo lastFrame() {
        return frameInfo;
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
        checkActive();
        if (rendering) {
            throw new IllegalStateException("Record into layers before render(), not while rendering.");
        }
        return state(layer).canvas;
    }

    /**
     * Render every pass in order, then clear the layers for the next frame.
     */
    public void render() {
        checkActive();
        if (rendering) {
            throw new IllegalStateException("render() called from inside a render pass.");
        }
        rendering = true;
        frame++;
        long start = System.nanoTime();
        frameInfo.begin(frame);
        try {
            int screenWidth = backend.getScreenWidth(), screenHeight = backend.getScreenHeight();
            screen[0] = screenWidth; // what target views measure pixels against until the next render()
            screen[1] = screenHeight;
            for (TargetState target : targets) {
                if (target.screenScale > 0f) {
                    target.target.resize(Builder.scaled(screenWidth, target.screenScale),
                            Builder.scaled(screenHeight, target.screenScale));
                }
            }
            for (LayerState layer : layers) {
                layer.canvas.flush();
            }
            for (Pass pass : passes) {
                long passStart = System.nanoTime();
                long drawCalls = backend.getDrawCallCount();
                try {
                    switch (pass.kind) {
                        case LAYERS -> renderLayers(pass);
                        case POST -> renderPost(pass);
                        default -> pass.custom.render(backend);
                    }
                } finally {
                    pass.stats.drawCalls = backend.getDrawCallCount() - drawCalls;
                    pass.stats.nanos = System.nanoTime() - passStart;
                }
            }
        } finally {
            try {
                for (LayerState layer : layers) {
                    layer.list.clear();
                    layer.canvas.reset();
                }
                frameInfo.end(System.nanoTime() - start);
            } finally {
                rendering = false;
            }
        }
    }

    private void renderLayers(Pass pass) {
        float[] cull = cullBounds(pass.view.getViewMatrix());
        RenderTarget target = pass.target != null ? pass.target.target : null;
        boolean clear = pass.target != null && pass.target.writtenFrame != frame;
        if (pass.target != null) {
            pass.target.writtenFrame = frame;
        }
        for (int l = 0; l < pass.layers.length; l++) {
            LayerState layer = pass.layers[l];
            FrameInfo.LayerStats stats = pass.layerStats[l];
            DrawList list = layer.list;
            layer.canvas.flush(); // anything recorded since the start of render() (e.g. by a custom pass)
            stats.commands = list.size();
            if (list.size() == 0) {
                continue;
            }
            // sort once per frame, even when several passes draw the layer (again if a custom pass added commands)
            if (layer.preparedFrame != frame || !list.isPrepared()) {
                list.prepare();
                layer.preparedFrame = frame;
                frameInfo.capturePicks(layer.layer, list); // what is drawn is what can be picked
            }
            stats.culled = batcher.build(list, backend.capabilities(), renderData, cull);
            BatchList batches = renderData.batches();
            if (batches.size() == 0) {
                continue; // everything culled
            }
            for (int b = 0, n = batches.size(); b < n; b++) {
                frameInfo.addBatch(stats, batches.reason(b), batches.material(b), batches.quadCount(b),
                        batches.textureCount(b));
            }
            passState.set(pass.view.getViewMatrix(), layer.layer.getBlendMode()).target(target, clear);
            clear = false;
            long drawCalls = backend.getDrawCallCount();
            backend.beginPass(passState);
            try {
                backend.submit(renderData);
            } finally {
                backend.endPass();
                stats.drawCalls = backend.getDrawCallCount() - drawCalls;
            }
        }
        if (clear) { // nothing drawn into the target this frame: still clear last frame's contents
            passState.set(pass.view.getViewMatrix(), BlendMode.NORMAL_BLEND).target(target, true);
            backend.beginPass(passState);
            backend.endPass();
        }
    }

    private void renderPost(Pass pass) {
        RenderTarget source = pass.source.target;
        RenderTarget destination = pass.target != null ? pass.target.target : null;
        PostEffect[] chain = pass.effects;
        Context context = pass.context; // scratch targets of this pass: sizes stay stable, nothing reallocates
        Texture input = source.getTexture();
        int count = Math.max(1, chain.length);
        for (int i = 0; i < count; i++) {
            PostEffect effect = chain.length > 0 ? chain[i] : copy;
            boolean last = i == count - 1;
            RenderTarget output = last ? destination
                    : context.chain(i % 2, source.getWidth(), source.getHeight());
            effect.apply(context, input, output, last && destination == null);
            if (output != null) {
                input = output.getTexture();
            }
        }
        if (pass.target != null) {
            pass.target.writtenFrame = frame; // fully written: no clear needed by later passes
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

    /**
     * @return Whether {@link #dispose()} was called.
     */
    public boolean isDisposed() {
        return disposed;
    }

    private void checkActive() {
        if (disposed) {
            throw new IllegalStateException("Render pipeline has been disposed.");
        }
    }

    /**
     * Release the pipeline's own targets and scratch targets and the effects it was built with.
     */
    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        if (rendering) {
            throw new IllegalStateException("A pipeline can't be disposed while it renders (e.g. from a custom pass).");
        }
        disposed = true;
        frameInfo.begin(0); // no references to the last frame's materials
        for (LayerState layer : layers) {
            layer.list.clear();
            layer.canvas.reset();
        }
        for (TargetState target : targets) {
            if (target.owned) {
                target.target.dispose();
            }
        }
        for (PostEffect effect : effects) {
            effect.dispose();
        }
        copy.dispose();
        for (Pass pass : passes) {
            if (pass.context != null) {
                pass.context.dispose();
            }
        }
    }

    // region model

    private enum Kind {LAYERS, POST, CUSTOM}

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

    private static final class TargetState {
        final RenderTarget target;
        final boolean owned;
        final float screenScale; // > 0: resized to the screen size times this every frame
        int writtenFrame;

        TargetState(RenderTarget target, boolean owned, float screenScale) {
            this.target = target;
            this.owned = owned;
            this.screenScale = screenScale;
        }
    }

    private static final class Pass {
        final Kind kind;
        View view;
        LayerState[] layers;
        TargetState target; // layer pass: drawn into; post pass: written to (null = screen)
        TargetState source;
        PostEffect[] effects;
        Context context; // post passes: their own scratch targets
        CustomPass custom;
        FrameInfo.PassStats stats;
        FrameInfo.LayerStats[] layerStats; // layer passes: one per layer

        Pass(Kind kind) {
            this.kind = kind;
        }
    }

    /**
     * A view drawing into a target: same matrix, pixels per unit measured against the target's size (for screen
     * targets, the size they are about to get, since canvases record before {@code render()} resizes them).
     */
    private static final class TargetView implements View {
        private final View view;
        private final TargetState target;
        private final int[] screen; // the pipeline's screen size, measured once per render()

        TargetView(View view, TargetState target, int[] screen) {
            this.view = view;
            this.target = target;
            this.screen = screen;
        }

        @Override
        public Matrix4 getViewMatrix() {
            return view.getViewMatrix();
        }

        @Override
        public float getPixelsPerUnit() {
            float[][] m = view.getViewMatrix().toUnsafeArray(); // clip units per layer unit, per axis
            float width = target.screenScale > 0f ? screen[0] * target.screenScale : target.target.getWidth();
            float height = target.screenScale > 0f ? screen[1] * target.screenScale : target.target.getHeight();
            // sqrt rather than Math.hypot (which guards against overflow at many times the cost): asked per command
            float sx = (float) Math.sqrt(m[0][0] * m[0][0] + m[0][1] * m[0][1]) * width * 0.5f;
            float sy = (float) Math.sqrt(m[1][0] * m[1][0] + m[1][1] * m[1][1]) * height * 0.5f;
            return Math.min(sx, sy);
        }
    }

    /**
     * Post effect context of one post pass: the backend plus its scratch targets (chain ping-pong and per-slot
     * temporaries).
     */
    private static final class Context implements PostContext {
        private final RenderBackend backend;
        private final RenderTarget[] chain = new RenderTarget[2];
        private RenderTarget[] temporaries = new RenderTarget[4];

        Context(RenderBackend backend) {
            this.backend = backend;
        }

        @Override
        public RenderBackend backend() {
            return backend;
        }

        @Override
        public void apply(PostEffect effect, Texture source, RenderTarget destination, boolean blend) {
            effect.apply(this, source, destination, blend);
        }

        @Override
        public RenderTarget temporary(int slot, int width, int height) {
            if (slot < 0) {
                throw new IllegalArgumentException("Slot must be positive.");
            }
            if (slot >= temporaries.length) {
                temporaries = java.util.Arrays.copyOf(temporaries, Math.max(slot + 1, temporaries.length * 2));
            }
            return sized(temporaries, slot, width, height);
        }

        RenderTarget chain(int slot, int width, int height) {
            return sized(chain, slot, width, height);
        }

        private RenderTarget sized(RenderTarget[] pool, int slot, int width, int height) {
            RenderTarget target = pool[slot];
            if (target == null) {
                target = pool[slot] = backend.createTarget(width, height);
            } else {
                target.resize(width, height);
            }
            return target;
        }

        void dispose() {
            for (RenderTarget target : chain) {
                if (target != null) {
                    target.dispose();
                }
            }
            for (RenderTarget target : temporaries) {
                if (target != null) {
                    target.dispose();
                }
            }
        }
    }

    /**
     * Builds a {@link RenderPipeline}; passes run in the order they are added.
     */
    public static final class Builder {
        private final RenderBackend backend;
        private final List<LayerState> layers = new ArrayList<>();
        private final List<TargetState> targets = new ArrayList<>();
        private final List<Pass> passes = new ArrayList<>();
        private final List<PostEffect> effects = new ArrayList<>();
        // screen size shared with the target views (canvases ask them for every command: no GPU query each time)
        private final int[] screen = new int[2];

        private Builder(RenderBackend backend) {
            this.backend = backend;
            screen[0] = backend.getScreenWidth();
            screen[1] = backend.getScreenHeight();
        }

        /**
         * A target that follows the screen size (times {@code scale}, e.g. 0.5 for a half-resolution pixel-art
         * scene), owned by the pipeline.
         */
        public RenderTarget screenTarget(float scale) {
            checkNotBuilt();
            if (!(scale > 0f)) {
                throw new IllegalArgumentException("Scale must be positive.");
            }
            RenderTarget target = backend.createTarget(scaled(backend.getScreenWidth(), scale),
                    scaled(backend.getScreenHeight(), scale)); // and resized by render() when the screen changes
            targets.add(new TargetState(target, true, scale));
            return target;
        }

        /**
         * A fixed-size target owned by the pipeline (e.g. a minimap).
         */
        public RenderTarget target(int width, int height) {
            checkNotBuilt();
            RenderTarget target = backend.createTarget(width, height);
            targets.add(new TargetState(target, true, 0f));
            return target;
        }

        /**
         * Draw layers through a view to the screen, in the given order.
         *
         * @param view   The view (e.g. a camera, or screen space for a HUD).
         * @param layers The layers (at least one; a layer may also appear in other passes).
         */
        public Builder pass(View view, Layer... layers) {
            return pass(view, null, layers);
        }

        /**
         * Draw layers through a view into a target ({@code null} = the screen), in the given order. The target is
         * cleared the first time a pass draws into it in a frame.
         */
        public Builder pass(View view, RenderTarget target, Layer... layers) {
            checkNotBuilt();
            if (view == null || layers == null || layers.length == 0) {
                throw new IllegalArgumentException("A pass needs a view and at least one layer.");
            }
            Pass pass = new Pass(Kind.LAYERS);
            pass.view = view;
            pass.target = target != null ? stateFor(target) : null;
            View scaled = target != null ? new TargetView(view, pass.target, screen) : view; // the target's pixels
            pass.layers = new LayerState[layers.length];
            for (int i = 0; i < layers.length; i++) {
                if (layers[i] == null) {
                    throw new IllegalArgumentException("Layers cannot be null.");
                }
                for (int j = 0; j < i; j++) {
                    if (layers[j] == layers[i]) {
                        throw new IllegalArgumentException(layers[i] + " is listed twice in one pass.");
                    }
                }
                pass.layers[i] = stateFor(layers[i], scaled);
            }
            passes.add(pass);
            return this;
        }

        /**
         * Run effects on a target and composite the result over the screen (no effects: a plain copy).
         */
        public Builder post(RenderTarget source, PostEffect... effects) {
            return post(source, null, effects);
        }

        /**
         * Run effects on a target and write the result to another target ({@code null} = composite over the
         * screen).
         */
        public Builder post(RenderTarget source, RenderTarget destination, PostEffect... effects) {
            checkNotBuilt();
            if (source == null) {
                throw new IllegalArgumentException("A post pass needs a source target.");
            }
            if (source == destination) {
                throw new IllegalArgumentException("A post pass can't write the target it reads.");
            }
            Pass pass = new Pass(Kind.POST);
            pass.source = stateFor(source);
            pass.target = destination != null ? stateFor(destination) : null;
            pass.effects = effects != null ? effects.clone() : new PostEffect[0];
            pass.context = new Context(backend);
            for (PostEffect effect : pass.effects) {
                if (effect == null) {
                    throw new IllegalArgumentException("Effects cannot be null.");
                }
                if (!this.effects.contains(effect)) {
                    this.effects.add(effect);
                }
            }
            passes.add(pass);
            return this;
        }

        /**
         * Run custom code with direct backend access at this point of the frame.
         */
        public Builder custom(CustomPass custom) {
            checkNotBuilt();
            if (custom == null) {
                throw new IllegalArgumentException("Custom pass cannot be null.");
            }
            Pass pass = new Pass(Kind.CUSTOM);
            pass.custom = custom;
            passes.add(pass);
            return this;
        }

        public RenderPipeline build() {
            checkNotBuilt();
            if (passes.isEmpty()) {
                throw new IllegalStateException("A pipeline needs at least one pass.");
            }
            for (LayerState state : layers) {
                state.canvas.setViews(state.views.toArray(new View[0]));
            }
            built = true; // the pipeline owns the builder's layers, canvases and targets from now on
            return new RenderPipeline(this);
        }

        private boolean built;

        private void checkNotBuilt() {
            if (built) {
                throw new IllegalStateException("This builder already built its pipeline: start a new one.");
            }
        }

        private static int scaled(int size, float scale) {
            return Math.max(1, Math.round(size * scale));
        }

        private TargetState stateFor(RenderTarget target) {
            for (TargetState state : targets) {
                if (state.target == target) {
                    return state;
                }
            }
            TargetState state = new TargetState(target, false, 0f); // created by the caller: theirs to dispose
            targets.add(state);
            return state;
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
