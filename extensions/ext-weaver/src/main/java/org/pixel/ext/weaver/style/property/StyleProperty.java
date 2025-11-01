package org.pixel.ext.weaver.style.property;

public record StyleProperty<T>(String name, Class<T> type, T defaultValue) {

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof StyleProperty<?> other)) return false;
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
