package org.pixel.ext.tiled.content.importer;

import org.pixel.content.ImportContext;
import org.pixel.ext.tiled.content.TileMap;
import org.pixel.ext.tiled.content.Layer;
import org.w3c.dom.Element;

public class NullLayerCollector extends LayerCollector {
    @Override
    public Layer collect(TileMap tileMap, Element tileLayerElement, ImportContext ctx) {
        return null;
    }
}
