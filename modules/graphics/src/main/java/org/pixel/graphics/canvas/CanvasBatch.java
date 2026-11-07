package org.pixel.graphics.canvas;

import org.pixel.commons.lifecycle.Disposable;
import org.pixel.graphics.backend.PassState;
import org.pixel.graphics.backend.RenderBackend;
import org.pixel.graphics.backend.RenderData;
import org.pixel.graphics.draw.Batcher;
import org.pixel.graphics.draw.DrawList;
import org.pixel.graphics.draw.LayerMode;
import org.pixel.graphics.render.BlendMode;
import org.pixel.math.Matrix4;

/**
 * Immediate-mode owner of a {@link Canvas}: {@code begin} returns the canvas, {@code end} renders what was
 * recorded (like {@code SpriteBatch}). Once the render pipeline exists (URP v2 Phase 3), pipeline layers own their
 * canvases and this class is for standalone use.
 */
public final class CanvasBatch implements Disposable {

    private final RenderBackend backend;
    private final DrawList drawList;
    private final Canvas canvas;
    private final Batcher batcher = new Batcher();
    private final RenderData renderData;
    private final PassState passState = new PassState();
    private boolean drawing;
    private boolean disposed;

    /**
     * Constructor (painter's order: later commands draw on top).
     *
     * @param backend The render backend of the graphics device.
     */
    public CanvasBatch(RenderBackend backend) {
        this(backend, LayerMode.ORDERED);
    }

    /**
     * Constructor.
     *
     * @param backend The render backend of the graphics device.
     * @param mode    {@link LayerMode#ORDERED} (painter's order) or {@link LayerMode#SORTED} (by depth).
     */
    public CanvasBatch(RenderBackend backend, LayerMode mode) {
        if (backend == null) {
            throw new IllegalArgumentException("Render backend cannot be null.");
        }
        this.backend = backend;
        this.drawList = new DrawList(mode);
        this.canvas = new Canvas(drawList);
        this.renderData = new RenderData(backend.capabilities().maxTextureSlots());
    }

    public Canvas begin(Matrix4 viewMatrix) {
        return begin(viewMatrix, BlendMode.NORMAL_BLEND);
    }

    public Canvas begin(Matrix4 viewMatrix, BlendMode blendMode) {
        if (disposed) {
            throw new IllegalStateException("CanvasBatch has been disposed.");
        }
        if (drawing) {
            throw new IllegalStateException("begin() called twice without end().");
        }
        passState.set(viewMatrix, blendMode);
        drawList.clear();
        canvas.reset();
        drawing = true;
        return canvas;
    }

    public void end() {
        if (!drawing) {
            throw new IllegalStateException("end() called without begin().");
        }
        drawing = false;
        canvas.flush();
        if (drawList.size() == 0) {
            return;
        }
        drawList.prepare();
        batcher.build(drawList, backend.capabilities(), renderData);
        backend.beginPass(passState);
        try {
            backend.submit(renderData);
        } finally {
            backend.endPass();
        }
        drawList.clear();
    }

    @Override
    public void dispose() {
        drawList.clear();
        drawing = false;
        disposed = true;
    }
}
