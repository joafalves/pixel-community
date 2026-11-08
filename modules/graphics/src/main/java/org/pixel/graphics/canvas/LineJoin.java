package org.pixel.graphics.canvas;

/**
 * How two segments of a stroked path are connected.
 */
public enum LineJoin {
    /**
     * Sharp corner (falls back to {@link #BEVEL} beyond the miter limit).
     */
    MITER,
    /**
     * Rounded corner.
     */
    ROUND,
    /**
     * Corner cut flat.
     */
    BEVEL
}
