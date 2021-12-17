/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.gui.style;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class StylePropertyValue {

    public static final String SEPARATOR_COMMA = ",";
    public static final String SEPARATOR_SPACE = " ";

    private String value;
    private String separator;
}
