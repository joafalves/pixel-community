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
     * offset 20: tex slot  u8, then 3 bytes reserved
     * </pre>
     */
    SPRITE(24),

    /**
     * Shapes and canvas images (SDF evaluated in the fragment shader). 52 bytes per vertex:
     * <pre>
     * offset  0: position    2 x f32  (layer space)
     * offset  8: texcoord    2 x f32  (texture coordinates, or shape dimensions for SDF shapes)
     * offset 16: color       4 x u8   (fill color, normalized RGBA)
     * offset 20: local       2 x f32  (shape-local coordinate of this corner)
     * offset 28: params      4 x f32  (shape parameters, e.g. corner radii)
     * offset 44: shape type  u8
     * offset 45: tex slot    u8
     * offset 46: clip        u16      (batch-local, 1-based; 0 = no clip)
     * offset 48: style       u16      (batch-local, 1-based; 0 = no style: stroke, gradient, shadow, material data)
     * offset 50: reserved    u16
     * </pre>
     */
    FULL(52);

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
