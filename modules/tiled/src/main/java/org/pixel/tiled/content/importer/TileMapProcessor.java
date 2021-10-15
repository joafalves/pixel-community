package org.pixel.tiled.content.importer;

import org.pixel.content.ImportContext;
import org.pixel.tiled.content.TileMap;
import org.w3c.dom.Document;

public interface TileMapProcessor {
    void process(TileMap tileMap, Document document, ImportContext ctx);
}
