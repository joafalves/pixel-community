package org.pixel.graphics.backend;

/**
 * Vertex formats produced by the batcher. Every batch uses exactly one layout.
 */
public enum VertexLayout {

    /**
     * Textured quads (sprites, bitmap-font glyphs). 24 bytes per vertex:
     * <pre>
     * offset  0: position  2 x f32
     * offset  8: texcoord  2 x f32
     * offset 16: color     4 x u8 (normalized, RGBA)
     * offset 20: tex slot  u8, then 3 bytes reserved (clip index in a later phase)
     * </pre>
     */
    SPRITE(24);

    private final int stride;

    VertexLayout(int stride) {
        this.stride = stride;
    }

    /**
     * @return Bytes per vertex.
     */
    public int stride() {
        return stride;
    }
}
