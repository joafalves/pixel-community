package org.pixel.ext.tiled.content.importer;

import org.pixel.content.ImportContext;
import org.pixel.ext.tiled.content.TiledLayer;
import org.pixel.ext.tiled.content.TiledMap;
import org.w3c.dom.Element;

class NullLayerCollector extends LayerCollector {
    @Override
    public TiledLayer collect(TiledMap tileMap, Element tileLayerElement, ImportContext ctx) {
        return null;
    }
}
