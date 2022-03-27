package org.pixel.ext.tiled.content.importer;

import java.util.HashMap;

class LayerCollectorFactory {
    private final static HashMap<String, LayerCollector> nameToStrategy = new HashMap<>();

    static {
        nameToStrategy.put("layer", new TileLayerCollector());
        nameToStrategy.put("objectgroup", new ObjectGroupCollector());
        nameToStrategy.put("group", new LayerGroupCollector());
        nameToStrategy.put("imagelayer", new ImageLayerCollector());
    }

    public LayerCollector getLayerCollector(String layerName) {
        return nameToStrategy.getOrDefault(layerName, new NullLayerCollector());
    }
}
