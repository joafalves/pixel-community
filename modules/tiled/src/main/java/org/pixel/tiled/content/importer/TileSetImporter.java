package org.pixel.tiled.content.importer;

import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.content.ContentImporter;
import org.pixel.content.ContentImporterInfo;
import org.pixel.content.ImportContext;
import org.pixel.content.Texture;
import org.pixel.content.importer.settings.ContentImporterSettings;
import org.pixel.tiled.content.TileSet;
import org.pixel.tiled.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

        return new TileSet(tileWidth, tileHeight, tileCount, columns, tileSetImage);
    }
}
