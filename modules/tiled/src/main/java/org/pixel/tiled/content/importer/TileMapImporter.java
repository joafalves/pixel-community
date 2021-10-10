/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.tiled.content.importer;

import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.content.*;
import org.pixel.tiled.content.TileMap;
import org.pixel.tiled.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

        return tileMap;
    }
}