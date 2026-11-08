package org.pixel.graphics.draw;

/**
 * How the commands of a {@link DrawList} are ordered before they are batched.
 */
public enum LayerMode {

    /**
     * Submission order (painter's algorithm). Nothing is reordered. Used for HUD/GUI/overlays.
     */
    ORDERED,

    /**
     * Ascending depth; commands with equal depth keep their submission order (stable). Used for the game world and
     * by the {@code SpriteBatch} compatibility path.
     */
    SORTED,

    /**
     * Ascending depth; within equal depth, commands are grouped by material and then by texture (their order among
     * each other is unspecified, but stable from frame to frame). Fewer batches when same-depth commands don't
     * overlap, or their overlap order doesn't matter: tiles, particles.
     */
    SORTED_BY_MATERIAL
}
