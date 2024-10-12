package org.pixel.commons;

import java.util.HashMap;
import java.util.List;

public class DataHashMap extends HashMap<String, Object> {

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
            return (T) o;
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
}
