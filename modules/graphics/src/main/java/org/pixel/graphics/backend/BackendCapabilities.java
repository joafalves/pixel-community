package org.pixel.graphics.backend;

/**
 * Limits of a {@link RenderBackend} that the batching code must respect.
 *
 * @param maxTextureSlots       Textures one batch may reference (bound to texture units at the same time).
 * @param maxQuadsPerBatch      Quads one draw call may contain (bounded by the index format).
 * @param maxClipsPerBatch      Clip rectangles one batch may reference (size of the clip uniform block).
 * @param maxStylesPerBatch     Styles one batch may reference (size of the style uniform block).
 * @param tableAlignmentFloats  Alignment (in floats) of each batch's table data inside the pass tables
 *                              (uniform buffer offset alignment / 4).
 */
public record BackendCapabilities(int maxTextureSlots, int maxQuadsPerBatch, int maxClipsPerBatch,
                                  int maxStylesPerBatch, int tableAlignmentFloats) {

    /**
     * Texture units reserved for a material's own textures ({@code Material.texture(name, texture)}).
     */
    public static final int MATERIAL_TEXTURES = 4;

    public BackendCapabilities {
        if (maxTextureSlots < 1 || maxQuadsPerBatch < 1 || maxClipsPerBatch < 1 || maxStylesPerBatch < 1) {
            throw new IllegalArgumentException("Backend limits must be at least 1.");
        }
        if (tableAlignmentFloats < 4 || tableAlignmentFloats % 4 != 0) {
            throw new IllegalArgumentException("tableAlignmentFloats must be a positive multiple of 4 (one vec4).");
        }
    }

    /**
     * Texture slots of a batch drawn with a material: the material's own textures take the last
     * {@link #MATERIAL_TEXTURES} units.
     */
    public int materialTextureSlots() {
        return Math.max(1, maxTextureSlots - MATERIAL_TEXTURES);
    }
}
