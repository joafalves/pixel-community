package org.pixel.commons.data;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A type-safe data container that uses a delegate Map<String, Object> internally.
 * This refactoring allows for backend flexibility and thread-safe variants.
 */
public class DataMap implements Map<String, Object> {

    protected final Map<String, Object> delegate;

    /**
     * Factory method to create a new DataMap instance with a ConcurrentHashMap as the delegate.
     * This is useful for thread-safe operations where multiple threads may access the map concurrently.
     *
     * @return A new DataMap instance.
     */
    public static DataMap createConcurrent() {
        return new DataMap(new ConcurrentHashMap<>());
    }

    /**
     * Default constructor that initializes the delegate map to a new HashMap.
     */
    public DataMap() {
        this.delegate = new HashMap<>();
    }

    /**
     * Constructor that accepts a delegate map.
     *
     * @param delegate The delegate map to use.
     */
    public DataMap(Map<String, Object> delegate) {
        this.delegate = delegate;
    }

    /**
     * Associates the specified value with the specified key in this map.
     * The key is automatically generated based on the package class name.
     * If the map previously contained a mapping for the key, the old
     * value is replaced.
     *
     * @param value value to be associated with the specified key
     * @return the previous value associated with {@code key}, or
     * {@code null} if there was no mapping for {@code key}.
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
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return delegate.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        return delegate.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return delegate.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        delegate.putAll(m);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public Set<String> keySet() {
        return delegate.keySet();
    }

    @Override
    public Collection<Object> values() {
        return delegate.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return delegate.entrySet();
    }

    /**
     * Returns a string representation of the map.
     *
     * @return A string in the form {key1=value1, key2=value2, ...}
     */
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