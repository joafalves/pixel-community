/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.gui.style;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class Style {

    private List<StyleSelector> styleSelectors;

}
