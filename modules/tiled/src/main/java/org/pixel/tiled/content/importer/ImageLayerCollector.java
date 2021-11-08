package org.pixel.tiled.content.importer;

import org.pixel.content.ImportContext;
import org.pixel.content.Texture;
import org.pixel.content.importer.settings.ContentImporterSettings;
import org.pixel.tiled.content.Layer;
import org.pixel.tiled.content.TileMap;
import org.pixel.tiled.content.TiledImageLayer;
import org.w3c.dom.Element;

public class ImageLayerCollector extends LayerCollector{
    @Override
    Layer collect(TileMap tileMap, Element tileLayerElement, ImportContext ctx) {
        TiledImageLayer layer = new TiledImageLayer(collectLayerData(tileMap, tileLayerElement));

        Element image = (Element) tileLayerElement.getElementsByTagName("image").item(0);

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
