/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.gui.style;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class StylePropertyValueFunction extends StylePropertyValue {

    private String functionName;
    private List<String> parameters;

    @Builder(builderMethodName = "functionBuilder")
    StylePropertyValueFunction(String value, String separator, String functionName, List<String> parameters) {
        super(value, separator);
        this.functionName = functionName;
        this.parameters = parameters;
    }
}
