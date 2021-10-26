package org.pixel.tiled.content.importer;

import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.content.ContentImporter;
import org.pixel.content.ContentImporterInfo;
import org.pixel.content.ImportContext;
import org.pixel.content.Texture;
import org.pixel.content.importer.settings.ContentImporterSettings;
import org.pixel.tiled.content.TileSet;
import org.pixel.tiled.content.TiledCustomProperties;
import org.pixel.tiled.content.TiledTile;
import org.pixel.tiled.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@ContentImporterInfo(type = TileSet.class, extension = ".tsx")
public class TileSetImporter implements ContentImporter<TileSet> {
    private static final Logger LOG = LoggerFactory.getLogger(TileSetImporter.class);

    @Override
    public TileSet process(ImportContext ctx) {
        XMLUtils utils = new XMLUtils();
        Document tsxDoc = utils.openXMLDocument(ctx);

        if (tsxDoc == null) {
            return null;
        }

        Element tilesetElement = (Element) tsxDoc.getElementsByTagName("tileset").item(0);

        int tileHeight = Integer.parseInt(tilesetElement.getAttribute("tileheight"));
        int tileWidth = Integer.parseInt(tilesetElement.getAttribute("tilewidth"));
        int columns = Integer.parseInt(tilesetElement.getAttribute("columns"));
        int tileCount = Integer.parseInt(tilesetElement.getAttribute("tilecount"));

        Element image = (Element) tilesetElement.getElementsByTagName("image").item(0);

        String textureFilePath = image.getAttribute("source");

        ContentImporterSettings settings;

        if (ctx.getSettings() instanceof TileMapImporterSettings) {
            settings = ((TileMapImporterSettings) ctx.getSettings()).getTextureImporterSettings();
        } else {
            settings = ctx.getSettings();
        }

        Texture tileSetImage = ctx.getContentManager().load(textureFilePath, Texture.class, settings);

        if (tileSetImage == null) {
            LOG.error("Something went wrong processing the Tile Set texture image.");

            return null;
        }

        TiledCustomProperties customProperties = new CustomPropertiesCollector().collect(tilesetElement);

        TileSet tileSet = new TileSet(tileWidth, tileHeight, tileCount, columns, tileSetImage);
        tileSet.setCustomProperties(customProperties);

        NodeList tiles = tilesetElement.getElementsByTagName("tile");

        for(int i = 0; i < tiles.getLength(); i++) {
            Element tileElement = (Element) tiles.item(i);

            TiledTile tile = new TiledTile();

            NodeList groups = tileElement.getElementsByTagName("objectgroup");

            if(groups.getLength() == 0) {
                continue;
            }

            int index = Integer.parseInt(tileElement.getAttribute("id"));
            tileSet.setTile(index, tile);
        }

        return tileSet;
    }
}
