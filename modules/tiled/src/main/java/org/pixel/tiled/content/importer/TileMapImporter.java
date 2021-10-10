/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.tiled.content.importer;

import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.content.*;
import org.pixel.tiled.content.TileMap;
import org.pixel.tiled.content.TileSet;
import org.pixel.tiled.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@ContentImporterInfo(type = TileMap.class, extension = ".tmx")
public class TileMapImporter implements ContentImporter<TileMap> {
    private static final Logger LOG = LoggerFactory.getLogger(TileMapImporter.class);

    @Override
    public TileMap process(ImportContext ctx) {
        XMLUtils utils = new XMLUtils();
        Document tmxDoc = utils.openXMLDocument(ctx);

        if(tmxDoc == null) {
            return null;
        }

        Element mapElement = tmxDoc.getDocumentElement();

        TileMap tileMap = new TileMap();

        tileMap.setHeight(Integer.parseInt(mapElement.getAttribute("height")));
        tileMap.setWidth(Integer.parseInt(mapElement.getAttribute("width")));

        NodeList tilesets = tmxDoc.getElementsByTagName("tileset");

        for(int i = 0; i < tilesets.getLength(); i++) {
            Node tilesetNode = tilesets.item(i);

            if(tilesetNode.getNodeType() == Node.ELEMENT_NODE) {
                Element tileset = (Element) tilesetNode;
                String tilesetSource = tileset.getAttribute("source");

                TileSet tileSet = ctx.getContentManager().load(tilesetSource, TileSet.class);

                if(tileSet == null) {
                    LOG.error("Error loading tileset");
                } else {
                    int firstGId = Integer.parseInt(tileset.getAttribute("firstgid"));

                    tileSet.setFirstGId(firstGId);
                    tileMap.addTileSet(tileSet);
                }
            }
        }

        return tileMap;
    }
}
