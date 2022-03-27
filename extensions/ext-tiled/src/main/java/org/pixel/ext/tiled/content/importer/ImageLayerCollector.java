package org.pixel.ext.tiled.content.importer;

import org.pixel.content.ImportContext;
import org.pixel.content.Texture;
import org.pixel.content.importer.settings.ContentImporterSettings;
import org.pixel.ext.tiled.content.TiledLayer;
import org.pixel.ext.tiled.content.TiledMap;
import org.pixel.ext.tiled.content.TiledImageLayer;
import org.w3c.dom.Element;

class ImageLayerCollector extends LayerCollector {
    @Override
    TiledLayer collect(TiledMap tileMap, Element tileLayerElement, ImportContext ctx) {
        TiledImageLayer layer = new TiledImageLayer(collectLayerData(tileMap, tileLayerElement));

        Element image = (Element) tileLayerElement.getElementsByTagName("image").item(0);

        if(image == null) {
            return null;
        }

        String textureFilePath = image.getAttribute("source");

        ContentImporterSettings settings;

        if (ctx.getSettings() instanceof TileMapImporterSettings) {
            settings = ((TileMapImporterSettings) ctx.getSettings()).getTextureImporterSettings();
        } else {
            settings = ctx.getSettings();
        }

        Texture layerImage = ctx.getContentManager().load(textureFilePath, Texture.class, settings);

        if (layerImage == null) {
            return null;
        }

        layer.setImage(layerImage);

        return layer;
    }
}
