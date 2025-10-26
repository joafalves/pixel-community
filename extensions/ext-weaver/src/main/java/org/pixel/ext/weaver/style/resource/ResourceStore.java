package org.pixel.ext.weaver.style.resource;

import java.util.HashMap;
import java.util.Map;

public class ResourceStore<T> {

    private final Map<String, T> data = new HashMap<>();

    public void put(String key, T value) {
        data.put(key, value);
    }

    public T get(String key) {
        return data.get(key);
    }
}
