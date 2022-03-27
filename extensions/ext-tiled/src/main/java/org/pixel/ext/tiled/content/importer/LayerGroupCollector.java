package org.pixel.ext.tiled.content.importer;

import org.pixel.content.ImportContext;
import org.pixel.ext.tiled.content.TiledLayer;
import org.pixel.ext.tiled.content.TiledLayerGroup;
import org.pixel.ext.tiled.content.TiledMap;
import org.w3c.dom.Element;

class LayerGroupCollector extends LayerCollector {
    LayerProcessor processor;

    public LayerGroupCollector() {
        this.processor = new LayerProcessor();
    }

    public LayerGroupCollector(LayerProcessor processor) {
        this.processor = processor;
    }

    @Override
    TiledLayer collect(TiledMap tileMap, Element tileLayerElement, ImportContext ctx) {
        TiledLayerGroup group = new TiledLayerGroup(collectLayerData(tileMap, tileLayerElement));

        group.setLayers(processor.processChildren(tileMap, tileLayerElement, ctx));

        return group;
    }
}
