/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.core;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class WindowDimensions {
    private int virtualWidth;
    private int virtualHeight;
    private int windowWidth;
    private int windowHeight;
    private int frameWidth;
    private int frameHeight;
    private float pixelRatio;
}
