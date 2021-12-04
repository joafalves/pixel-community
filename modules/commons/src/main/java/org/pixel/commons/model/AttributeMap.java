package org.pixel.commons.model;

import java.util.HashMap;

public class AttributeMap extends HashMap<String, Object> {

    public <T> T get(String key, Class<T> type) {
        return get(key, type, null);
    }

    public <T> T get(String key, Class<T> type, T defaultValue) {
        Object o = get(key);
        if (type.isInstance(o)) {
            return (T) o;
        }

        return defaultValue;
    }

    public String getString(String key) {
        return get(key, String.class, null);
    }

    public String getString(String key, String defaultValue) {
        return get(key, String.class, defaultValue);
    }

    public Integer getInteger(String key) {
        return get(key, Integer.class, null);
    }

    public Integer getInteger(String key, Integer defaultValue) {
        return get(key, Integer.class, defaultValue);
    }

    public Float getFloat(String key) {
        return get(key, Float.class, null);
    }

    public Float getFloat(String key, Float defaultValue) {
        return get(key, Float.class, defaultValue);
    }

    public Double getDouble(String key) {
        return get(key, Double.class, null);
    }

    public Double getDouble(String key, Double defaultValue) {
        return get(key, Double.class, defaultValue);
    }

    public Long getLong(String key) {
        return get(key, Long.class, null);
    }

    public Long getLong(String key, Long defaultValue) {
        return get(key, Long.class, defaultValue);
    }

    public Number getNumber(String key) {
        return get(key, Number.class, null);
    }

    public Number getNumber(String key, Number defaultValue) {
        return get(key, Number.class, defaultValue);
    }

    public Boolean getBoolean(String key) {
        return get(key, Boolean.class, null);
    }

    public Boolean getBoolean(String key, Boolean defaultValue) {
        return get(key, Boolean.class, defaultValue);
    }
}
