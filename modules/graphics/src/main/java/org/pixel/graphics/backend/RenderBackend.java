package org.pixel.graphics.backend;

import org.pixel.commons.lifecycle.Disposable;

/**
 * The only layer that talks to the GPU API. Everything above it (draw lists, batching, front-ends) is GL-free.
 * <p>
 * Usage per pass: {@link #beginPass(PassState)}, one or more {@link #submit(VertexStream, BatchList)}, then
 * {@link #endPass()}. Implementations record their work into the device's render statistics.
 */
public interface RenderBackend extends Disposable {

    /**
     * @return The limits batching must respect.
     */
    BackendCapabilities capabilities();

    /**
     * Start a pass: bind the program, apply the view matrix and blend mode.
     *
     * @param state The pass state.
     */
    void beginPass(PassState state);

    /**
     * Upload the vertices and draw every batch, in order.
     *
     * @param vertices The vertex data of the pass.
     * @param batches  The batches (quad ranges + textures) to draw.
     */
    void submit(VertexStream vertices, BatchList batches);

    /**
     * End the pass and restore shared GPU state other renderers rely on.
     */
    void endPass();
}
