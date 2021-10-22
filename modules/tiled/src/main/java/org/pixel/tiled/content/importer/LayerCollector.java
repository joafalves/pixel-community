package org.pixel.tiled.content.importer;

import org.pixel.graphics.render.SpriteBatch;
import org.pixel.tiled.content.Layer;
import org.pixel.tiled.content.TileMap;
import org.pixel.tiled.content.TiledCustomProperties;
import org.pixel.tiled.view.TileMapView;
import org.w3c.dom.Element;

public abstract class LayerCollector {
    abstract Layer process(TileMap tileMap, Element tileLayerElement);

    Layer collectLayerData(TileMap tileMap, Element tileLayerElement) {
        CustomPropertiesCollector collector = new CustomPropertiesCollector();
        Layer layer = new Layer(tileMap) {
            @Override
            public void draw(SpriteBatch spriteBatch, TileMapView view) {

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
