package org.pixel.ext.tiled.content.importer;

import org.pixel.content.ImportContext;
import org.pixel.ext.tiled.content.Layer;
import org.pixel.ext.tiled.content.LayerGroup;
import org.pixel.ext.tiled.content.TileMap;
import org.w3c.dom.Element;

public class LayerGroupCollector extends LayerCollector {
    LayerProcessor processor;

    public LayerGroupCollector() {
        this.processor = new LayerProcessor();
    }

    public LayerGroupCollector(LayerProcessor processor) {
        this.processor = processor;
    }

    @Override
    Layer collect(TileMap tileMap, Element tileLayerElement, ImportContext ctx) {
        LayerGroup group = new LayerGroup(collectLayerData(tileMap, tileLayerElement));

        group.setLayers(processor.processChildren(tileMap, tileLayerElement, ctx));

        return group;
    }
}
