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
    /** Light accumulation (glows, fire, particles): {@code dst + src * a}; alpha still fades and anti-aliases. */
    ADDITIVE,
    /** Darkening (shadows, tints): {@code src * dst}. Alpha is not applied, so edges are not anti-aliased. */
    MULTIPLY
}
