package org.pixel.ext.tiled.content.importer;

import org.pixel.content.ImportContext;
import org.pixel.ext.tiled.content.TileMap;
import org.pixel.ext.tiled.content.TiledCustomProperties;
import org.pixel.ext.tiled.view.GenericTileMapView;
import org.pixel.ext.tiled.content.Layer;
import org.w3c.dom.Element;

public abstract class LayerCollector {
    abstract Layer collect(TileMap tileMap, Element tileLayerElement, ImportContext ctx);

    Layer collectLayerData(TileMap tileMap, Element tileLayerElement) {
        CustomPropertiesCollector collector = new CustomPropertiesCollector();
        Layer layer = new Layer(tileMap) {
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
