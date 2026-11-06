package org.pixel.graphics.backend;

/**
 * Limits of a {@link RenderBackend} that the batching code must respect.
 *
 * @param maxTextureSlots Textures one batch may reference (bound to texture units at the same time).
 * @param maxQuadsPerBatch Quads one draw call may contain (bounded by the index format).
 */
public record BackendCapabilities(int maxTextureSlots, int maxQuadsPerBatch) {

    public BackendCapabilities {
        if (maxTextureSlots < 1) {
            throw new IllegalArgumentException("maxTextureSlots must be at least 1.");
        }
        if (maxQuadsPerBatch < 1) {
            throw new IllegalArgumentException("maxQuadsPerBatch must be at least 1.");
        }
    }
}
