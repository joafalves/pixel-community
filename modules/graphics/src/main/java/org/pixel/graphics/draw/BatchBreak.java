package org.pixel.graphics.draw;

/**
 * Why the {@link Batcher} started a new batch (one more draw call). Shown by the pipeline's frame statistics, to find
 * out what to change for fewer draw calls.
 */
public enum BatchBreak {

    /** The first batch of a layer in a pass (not a break). */
    FIRST("first batch"),
    /** Sprite quads and shape quads alternate (different vertex layouts and programs). */
    LAYOUT("sprites and shapes alternate"),
    /** The material changed (including from or to the built-in shader). */
    MATERIAL("material changed"),
    /** The batch reached the most quads one draw call can index. */
    QUAD_LIMIT("quad limit"),
    /** A new texture didn't fit in the batch's texture slots. */
    TEXTURE_SLOTS("texture slots full"),
    /** A new clip rectangle didn't fit in the batch's clip table. */
    CLIP_TABLE("clip table full"),
    /** A new style (stroke, gradient, shadow, material data) didn't fit in the batch's style table. */
    STYLE_TABLE("style table full");

    private final String description;

    BatchBreak(String description) {
        this.description = description;
    }

    /**
     * @return A short human-readable reason, e.g. "texture slots full".
     */
    public String getDescription() {
        return description;
    }
}
