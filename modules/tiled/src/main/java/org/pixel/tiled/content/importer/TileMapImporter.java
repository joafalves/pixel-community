/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.tiled.content.importer;

import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.commons.util.IOUtils;
import org.pixel.commons.util.TextUtils;
import org.pixel.content.*;
import org.pixel.tiled.content.TileMap;
import org.pixel.tiled.content.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.swing.text.AbstractDocument;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

@ContentImporterInfo(type = TileMap.class, extension = ".tmx")
public class TileMapImporter implements ContentImporter<TileMap> {
    PixelContentManager contentManager;
    private static final Logger LOG = LoggerFactory.getLogger(TileMapImporter.class);

    public TileMapImporter(PixelContentManager contentManager) {
        this.contentManager = contentManager;
    }

    public TileMapImporter() {
        this.contentManager = new ContentManager();
    }

    @Override
    public TileMap process(ImportContext ctx) {
        XMLUtils utils = new XMLUtils();
        Document tmxDoc = utils.openXMLDocument(ctx);

        Element mapElement = tmxDoc.getDocumentElement();

        TileMap tileMap = new TileMap();

        tileMap.setHeight(Integer.parseInt(mapElement.getAttribute("height")));
        tileMap.setWidth(Integer.parseInt(mapElement.getAttribute("width")));

        return tileMap;
    }
}
