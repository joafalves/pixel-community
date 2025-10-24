package org.pixel.ext.weaver.style;

import java.util.Map;

/**
 * Class representing the computed style of a UI component.
 */
public class Style {

    // It now stores 'Object' to hold Color, Float, etc.
    private final Map<String, Object> properties;

    public static final Style EMPTY = new Style(Map.of());

    public Style(Map<String, Object> nativeProperties) {
        this.properties = nativeProperties;
    }

    /**
     * Gets a property using a typed StyleProperty with its default value as fallback.
     * This is the preferred method for type-safe property access.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(StyleProperty<T> property) {
        return (T) properties.getOrDefault(property.name(), property.defaultValue());
    }

    /**
     * Gets a property using a typed StyleProperty with a custom fallback value.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(StyleProperty<T> property, T defaultValue) {
        return (T) properties.getOrDefault(property.name(), defaultValue);
    }

    /**
     * Get a property as a raw Object.
     */
    public Object getRaw(String propertyName) {
        return properties.get(propertyName);
    }
}