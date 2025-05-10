package org.pixel.network.message;

import java.util.HashMap;
import java.util.Map;

public abstract class KeyValueMessage extends NetworkMessage {

    private final Map<String, String> keyValueMap = new HashMap<>();
    private byte[] cachedPayload;
    private boolean dirty = true;

    public KeyValueMessage(NetworkMessageType type) {
        super(type);
    }

    public void add(String key, String value) {
        keyValueMap.put(key, value);
        dirty = true;
    }

    public String get(String key) {
        return keyValueMap.get(key);
    }

    public boolean contains(String key) {
        return keyValueMap.containsKey(key);
    }

    public void remove(String key) {
        keyValueMap.remove(key);
        dirty = true;
    }

    public void clear() {
        keyValueMap.clear();
        dirty = true;
    }

    public int size() {
        return keyValueMap.size();
    }

    @Override
    public byte[] getPayload() {
        if (dirty || cachedPayload == null) {
            StringBuilder builder = new StringBuilder();
            keyValueMap.forEach((key, value) -> builder.append(key).append("=").append(value).append(";"));
            cachedPayload = builder.toString().getBytes();
            dirty = false;
        }
        return cachedPayload;
    }
}