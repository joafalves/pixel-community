/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.render;

/**
 * How drawn colors combine with what is already there.
 */
public enum BlendMode {
    /** Alpha blending: {@code src * a + dst * (1 - a)}. */
    NORMAL_BLEND,
    /**
     * Light accumulation (glows, fire, particles): {@code dst + src * a}; alpha still fades and anti-aliases. Light
     * doesn't cover what is behind it: drawn into a transparent target, it stays light when the target is drawn.
     */
    ADDITIVE,
    /** Darkening (shadows, tints): {@code dst * src}, weighted by alpha (soft edges and fades work). */
    MULTIPLY
}
