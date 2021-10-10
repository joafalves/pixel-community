package org.pixel.tiled.content.importer;

import org.pixel.content.ContentImporter;
import org.pixel.content.ContentImporterInfo;
import org.pixel.content.ImportContext;
import org.pixel.tiled.content.TileSet;
import org.pixel.tiled.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@ContentImporterInfo(type = TileSet.class, extension = ".tsx")
public class TileSetImporter implements ContentImporter<TileSet> {
    @Override
    public TileSet process(ImportContext ctx) {
        XMLUtils utils = new XMLUtils();
        Document tsxDoc = utils.openXMLDocument(ctx);

        if(tsxDoc == null) {
            return null;
        }

        Element tilesetElement = (Element) tsxDoc.getElementsByTagName("tileset").item(0);

        int tileHeight = Integer.parseInt(tilesetElement.getAttribute("tileheight"));
        int tileWidth = Integer.parseInt(tilesetElement.getAttribute("tilewidth"));
        int columns = Integer.parseInt(tilesetElement.getAttribute("columns"));
        int tileCount = Integer.parseInt(tilesetElement.getAttribute("tilecount"));

        TileSet tileSet = new TileSet(tileWidth, tileHeight, tileCount, columns);

        return tileSet;
    }
}
