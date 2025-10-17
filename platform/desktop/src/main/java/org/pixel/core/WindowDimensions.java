/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.core;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class WindowDimensions {
    private int viewportWidth;
    private int viewportHeight;
    private int windowWidth;
    private int windowHeight;
    private float pixelRatio;
}
