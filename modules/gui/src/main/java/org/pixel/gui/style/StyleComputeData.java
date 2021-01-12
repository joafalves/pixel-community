/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.gui.style;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
class StyleComputeData {
    private int level;
    private List<StyleProperty> properties;
}
