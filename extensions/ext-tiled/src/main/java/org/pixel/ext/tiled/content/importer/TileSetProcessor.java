package org.pixel.ext.tiled.content.importer;

import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.content.ImportContext;
import org.pixel.ext.tiled.content.TiledMap;
import org.pixel.ext.tiled.content.TiledTileSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class TileSetProcessor implements TileMapProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(TileMapImporter.class);

    @Override
    public void process(TiledMap tileMap, Document tmxDoc, ImportContext ctx) {
        NodeList tilesets = tmxDoc.getElementsByTagName("tileset");

        for (int i = 0; i < tilesets.getLength(); i++) {
            Node tilesetNode = tilesets.item(i);

            if (tilesetNode.getNodeType() == Node.ELEMENT_NODE) {
                Element tilesetElement = (Element) tilesetNode;
                String tilesetSource = tilesetElement.getAttribute("source");

                TiledTileSet tileSet = ctx.getContentManager().load(tilesetSource, TiledTileSet.class, ctx.getSettings());

                if (tileSet == null) {
                    LOG.error("Error loading tileset");
                } else {
                    int firstGId = Integer.parseInt(tilesetElement.getAttribute("firstgid"));

                    tileSet.setFirstGId(firstGId);
                    tileMap.addTileSet(tileSet);
                }
            }
        }
    }
}
