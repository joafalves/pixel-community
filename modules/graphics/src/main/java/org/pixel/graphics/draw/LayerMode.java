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
    SORTED
}
