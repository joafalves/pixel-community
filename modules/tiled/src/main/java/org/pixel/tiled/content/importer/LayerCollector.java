package org.pixel.tiled.content.importer;

import org.pixel.tiled.content.TileMap;
import org.w3c.dom.Element;

public interface LayerCollector {
    void process(TileMap tileMap, Element tileLayerElement);
}
