/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.ext.tiled.content.importer;

import org.pixel.content.ContentImporter;
import org.pixel.content.ContentImporterInfo;
import org.pixel.content.ImportContext;
import org.pixel.ext.tiled.content.TiledMap;
import org.pixel.ext.tiled.utils.XMLUtils;
import org.pixel.ext.tiled.view.TiledDrawStrategyFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@ContentImporterInfo(type = TiledMap.class, extension = ".tmx")
public class TiledMapImporter implements ContentImporter<TiledMap> {
    @Override
    public TiledMap process(ImportContext ctx) {
        XMLUtils utils = new XMLUtils();
        Document tmxDoc = utils.openXMLDocument(ctx);

        if (tmxDoc == null) {
            return null;
        }

        Element mapElement = tmxDoc.getDocumentElement();

        TiledMap tileMap = new TiledMap();

        tileMap.setHeight(Integer.parseInt(mapElement.getAttribute("height")));
        tileMap.setWidth(Integer.parseInt(mapElement.getAttribute("width")));
        tileMap.setTileHeight(Integer.parseInt(mapElement.getAttribute("tileheight")));
        tileMap.setTileWidth(Integer.parseInt(mapElement.getAttribute("tilewidth")));
        tileMap.setRenderOrder(mapElement.getAttribute("renderorder"));

        TiledDrawStrategyFactory drawStrategyFactory = new TiledDrawStrategyFactory();

        tileMap.setDrawStrategy(drawStrategyFactory.getDrawStrategy(tileMap));

        tileMap.setCustomProperties(new CustomPropertiesCollector().collect(mapElement));

        TiledMapImporterSettings importerSettings;

        if (ctx.getSettings() instanceof TiledMapImporterSettings) {
            importerSettings = (TiledMapImporterSettings) ctx.getSettings();
        } else {
            importerSettings = new TiledMapImporterSettings();

            ctx = new ImportContext(ctx.getContentManager(), ctx.getBuffer(), ctx.getFilepath(), importerSettings);
        }

        for (TileMapProcessor processor : importerSettings.getProcessors()) {
            processor.process(tileMap, tmxDoc, ctx);
        }

        return tileMap;
    }
}
