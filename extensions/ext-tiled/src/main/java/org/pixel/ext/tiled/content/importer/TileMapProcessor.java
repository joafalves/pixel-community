package org.pixel.ext.tiled.content.importer;

import org.pixel.content.ImportContext;
import org.pixel.ext.tiled.content.TiledMap;
import org.w3c.dom.Document;

public interface TileMapProcessor {
    void process(TiledMap tileMap, Document document, ImportContext ctx);
}
