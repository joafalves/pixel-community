package org.pixel.ext.rune.style;

import lombok.Getter;

/**
 * Type-safe style property definition.
 * Similar to CSS properties but with compile-time type checking.
 *
 * @param <T> The value type of this property
 */
@Getter
public class StyleProperty<T> {
    private final String name;
    private final Class<T> type;
    private final T defaultValue;

    public StyleProperty(String name, Class<T> type, T defaultValue) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof StyleProperty)) return false;
        StyleProperty<?> other = (StyleProperty<?>) obj;
        return name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
