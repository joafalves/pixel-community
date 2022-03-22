package org.pixel.ext.tiled.content.importer;

import java.util.HashMap;

public class ObjectOrderStrategyFactory {
    private final static HashMap<String, ObjectOrderStrategy> objectOrderToStrategy = new HashMap<>();

    static {
        objectOrderToStrategy.put("topdown", new TopDownObjectOrderStrategy());
        objectOrderToStrategy.put("index", new IndexObjectOrderStrategy());
    }

    public ObjectOrderStrategy getObjectOrderStrategy(String objectOrder) {
        return objectOrderToStrategy.getOrDefault(objectOrder, new TopDownObjectOrderStrategy());
    }
}
