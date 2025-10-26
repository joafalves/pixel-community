package org.pixel.ext.weaver.style.property.model;

import org.pixel.ext.weaver.style.property.type.MeasurementType;

public record Measurement(float value, MeasurementType type) {
    public static Measurement defaultValue() {
        return new Measurement(0, MeasurementType.PIXEL);
    }
}
