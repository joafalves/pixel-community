package org.pixel.graphics.text;

/**
 * Which vertical point of a text block is placed at the given y coordinate.
 */
public enum TextBaseline {
    /** Top of the block (the first line's ascender). */
    TOP,
    /** Middle of the block. */
    MIDDLE,
    /** Baseline of the first line. */
    BASELINE,
    /** Bottom of the block (the last line's descender). */
    BOTTOM
}
