package org.pixel.commons.data;

import java.util.HashMap;
import java.util.List;

public class DataMap extends HashMap<String, Object> {

    /**
     * Associates the specified value with the specified key in this map.
     * The key is automatically generated based on the package class name.
     * If the map previously contained a mapping for the key, the old
     * value is replaced.
     *
     * @param value value to be associated with the specified key
     * @return the previous value associated with {@code key}, or
     *         {@code null} if there was no mapping for {@code key}.
     *         (A {@code null} return can also indicate that the map
     *         previously associated {@code null} with {@code key}.)
     */
    public Object put(Object value) {
        return put(value.getClass().getPackageName(), value);
    }

    /**
     * Get the value (first element) of the given type. This function
     * uses the class package-name as key (convention).
     *
     * @param type The type of the value.
     * @return The value or null if not found.
     */
    public <T> T get(Class<T> type) {
        // attempt to use the package-name of the class as per convention:
        var value = get(type.getPackageName(), type);
        if (value != null) {
            return value;
        }

        // Couldn't find, try to do a manual lookup (first-find):
        for (Object o : this.values()) {
            if (type.isInstance(o)) {
                return type.cast(o);
            }
        }
        return null;
    }

    /**
     * Get the value of the key. If the key is not found, null is returned.
     *
     * @param key  The key.
     * @param type The type of the value.
     * @return The value of the key.
     */
    public <T> T get(String key, Class<T> type) {
        return get(key, type, null);
    }

    /**
     * Get the value of the key. If the key is not found, the default value is returned.
     *
     * @param key          The key.
     * @param type         The type of the value.
     * @param defaultValue The default value.
     * @return The value of the key.
     */
    public <T> T get(String key, Class<T> type, T defaultValue) {
        Object o = get(key);
        if (type.isInstance(o)) {
            return type.cast(o);
        }

        return defaultValue;
    }

    /**
     * Get the value of the key as a String. If the key is not found, null is returned.
     *
     * @return The value of the key as a String.
     */
    public String getString(String key) {
        return get(key, String.class, null);
    }

    /**
     * Get the value of the key as a String. If the key is not found, the default value is returned.
     *
     * @return The value of the key as a String.
     */
    public String getString(String key, String defaultValue) {
        return get(key, String.class, defaultValue);
    }

    /**
     * Get the value of the key as an Integer. If the key is not found, null is returned.
     *
     * @param key The key.
     * @return The value of the key as an Integer.
     */
    public Integer getInteger(String key) {
        return get(key, Integer.class, null);
    }

    /**
     * Get the value of the key as an Integer. If the key is not found, the default value is returned.
     *
     * @param key          The key.
     * @param defaultValue The default value.
     * @return The value of the key as an Integer.
     */
    public Integer getInteger(String key, Integer defaultValue) {
        return get(key, Integer.class, defaultValue);
    }

    /**
     * Get the value of the key as a Float. If the key is not found, null is returned.
     *
     * @param key The key.
     * @return The value of the key as a Float.
     */
    public Float getFloat(String key) {
        return get(key, Float.class, null);
    }

    /**
     * Get the value of the key as a Float. If the key is not found, the default value is returned.
     *
     * @param key          The key.
     * @param defaultValue The default value.
     * @return The value of the key as a Float.
     */
    public Float getFloat(String key, Float defaultValue) {
        return get(key, Float.class, defaultValue);
    }

    /**
     * Get the value of the key as a Double. If the key is not found, null is returned.
     *
     * @param key The key.
     * @return The value of the key as a Double.
     */
    public Double getDouble(String key) {
        return get(key, Double.class, null);
    }

    /**
     * Get the value of the key as a Double. If the key is not found, the default value is returned.
     *
     * @param key          The key.
     * @param defaultValue The default value.
     * @return The value of the key as a Double.
     */
    public Double getDouble(String key, Double defaultValue) {
        return get(key, Double.class, defaultValue);
    }

    /**
     * Get the value of the key as a Long. If the key is not found, null is returned.
     *
     * @param key The key.
     * @return The value of the key as a Long.
     */
    public Long getLong(String key) {
        return get(key, Long.class, null);
    }

    /**
     * Get the value of the key as a Long. If the key is not found, the default value is returned.
     *
     * @param key          The key.
     * @param defaultValue The default value.
     * @return The value of the key as a Long.
     */
    public Long getLong(String key, Long defaultValue) {
        return get(key, Long.class, defaultValue);
    }

    /**
     * Get the value of the key as a Number. If the key is not found, null is returned.
     *
     * @param key The key.
     * @return The value of the key as a Number.
     */
    public Number getNumber(String key) {
        return get(key, Number.class, null);
    }

    /**
     * Get the value of the key as a Number. If the key is not found, the default value is returned.
     *
     * @param key          The key.
     * @param defaultValue The default value.
     * @return The value of the key as a Number.
     */
    public Number getNumber(String key, Number defaultValue) {
        return get(key, Number.class, defaultValue);
    }

    /**
     * Get the value of the key as a Boolean. If the key is not found, null is returned.
     *
     * @param key The key.
     * @return The value of the key as a Boolean.
     */
    public Boolean getBoolean(String key) {
        return get(key, Boolean.class, null);
    }

    /**
     * Get the value of the key as a Boolean. If the key is not found, the default value is returned.
     *
     * @param key          The key.
     * @param defaultValue The default value.
     * @return The value of the key as a Boolean.
     */
    public Boolean getBoolean(String key, Boolean defaultValue) {
        return get(key, Boolean.class, defaultValue);
    }

    /**
     * Get the value of the key as a List. If the key is not found, null is returned.
     *
     * @param key The key.
     * @return The value of the key as a List.
     */
    public List<?> getList(String key) {
        return get(key, List.class, null);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (String key : this.keySet()) {
            sb.append(key).append("=").append(this.get(key)).append(", ");
        }
        if (sb.length() > 1) {
            sb.setLength(sb.length() - 2); // remove last comma and space
        }
        sb.append("}");
        return sb.toString();
    }
}
