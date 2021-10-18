package org.pixel.tiled.content.importer;

import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.content.ImportContext;
import org.pixel.tiled.content.TileMap;
import org.pixel.tiled.content.TileSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TileSetProcessor implements TileMapProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(TileMapImporter.class);

    @Override
    public void process(TileMap tileMap, Document tmxDoc, ImportContext ctx) {
        NodeList tilesets = tmxDoc.getElementsByTagName("tileset");

        for (int i = 0; i < tilesets.getLength(); i++) {
            Node tilesetNode = tilesets.item(i);

            if (tilesetNode.getNodeType() == Node.ELEMENT_NODE) {
                Element tileset = (Element) tilesetNode;
                String tilesetSource = tileset.getAttribute("source");

                TileSet tileSet = ctx.getContentManager().load(tilesetSource, TileSet.class, ctx.getSettings());

                if (tileSet == null) {
                    LOG.error("Error loading tileset");
                } else {
                    int firstGId = Integer.parseInt(tileset.getAttribute("firstgid"));

                    tileSet.setFirstGId(firstGId);
                    tileMap.addTileSet(tileSet);
                }
            }
        }
    }
}
