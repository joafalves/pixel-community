package org.pixel.ext.tiled.content.importer;

import org.pixel.content.ImportContext;
import org.pixel.ext.tiled.content.TiledLayer;
import org.pixel.ext.tiled.content.TiledMap;
import org.pixel.ext.tiled.content.TiledCustomProperties;
import org.pixel.ext.tiled.view.GenericTileMapView;
import org.w3c.dom.Element;

public abstract class LayerCollector {
    abstract TiledLayer collect(TiledMap tileMap, Element tileLayerElement, ImportContext ctx);

    TiledLayer collectLayerData(TiledMap tileMap, Element tileLayerElement) {
        CustomPropertiesCollector collector = new CustomPropertiesCollector();
        TiledLayer layer = new TiledLayer(tileMap) {
            @Override
            public void draw(GenericTileMapView view) {

            }
        };

        TiledCustomProperties customProperties = collector.collect(tileLayerElement);

        try {
            layer.setOffsetX(Float.parseFloat(tileLayerElement.getAttribute("offsetx")));
        } catch (NumberFormatException e) {
            layer.setOffsetX(0);
        }

        try {
            layer.setOffsetY(Float.parseFloat(tileLayerElement.getAttribute("offsety")));
        } catch (NumberFormatException e) {
            layer.setOffsetY(0);
        }

        layer.setCustomProperties(customProperties);

        return layer;
    }
}
