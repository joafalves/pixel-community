package org.pixel.graphics.backend;

/**
 * Everything a backend needs to draw one layer in a pass: one vertex stream per layout and the batches that reference them.
 * Reused across frames.
 */
public final class RenderData {

    private final VertexStream sprites = new VertexStream(VertexLayout.SPRITE, 4096);
    private final VertexStream shapes = new VertexStream(VertexLayout.FULL, 1024);
    private final BatchList batches;

    /**
     * Constructor.
     *
     * @param maxTextureSlots The maximum number of textures one batch may bind.
     */
    public RenderData(int maxTextureSlots) {
        this.batches = new BatchList(maxTextureSlots);
    }

    /**
     * @param layout The layout.
     * @return The vertex stream holding the vertices of batches with that layout.
     */
    public VertexStream vertices(VertexLayout layout) {
        return layout == VertexLayout.SPRITE ? sprites : shapes;
    }

    public BatchList batches() {
        return batches;
    }

    /**
     * Remove all vertices and batches (capacity is kept).
     */
    public void clear() {
        sprites.clear();
        shapes.clear();
        batches.clear();
    }
}
