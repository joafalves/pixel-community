package org.pixel.tiled.content.importer;

import org.pixel.tiled.content.TileLayer;
import org.pixel.tiled.content.TileMap;
import org.pixel.tiled.content.TiledCustomProperties;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Arrays;
import java.util.List;

public class TileLayerCollector implements LayerCollector {
    @Override
    public void process(TileMap tileMap, Element tileLayerElement) {
        int width = Integer.parseInt(tileLayerElement.getAttribute("width"));
        int height = Integer.parseInt(tileLayerElement.getAttribute("height"));
        double offsetX, offsetY;
        try {
            offsetX = Double.parseDouble(tileLayerElement.getAttribute("offsetx"));
        } catch (NumberFormatException e) {
            offsetX = 0;
        }

        try {
            offsetY = Double.parseDouble(tileLayerElement.getAttribute("offsety"));
        } catch (NumberFormatException e) {
            offsetY = 0;
        }

        TileLayer layer = new TileLayer(width, height, tileMap);
        layer.setOffsetX(offsetX);
        layer.setOffsetY(offsetY);

        Node dataNode = tileLayerElement.getElementsByTagName("data").item(0);

        if (dataNode.getNodeType() == Node.ELEMENT_NODE) {
            Element dataElement = (Element) dataNode;

            String data = dataElement.getTextContent();

            data = data.replace("\n", "");

            List<String> numbers = Arrays.asList(data.trim().split(","));

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    layer.addTile(x, y, Long.parseLong(numbers.get(y * width + x)));
                }
            }
        }

        CustomPropertiesCollector collector = new CustomPropertiesCollector();

        TiledCustomProperties customProperties = collector.collect(tileLayerElement);
        layer.setCustomProperties(customProperties);

        tileMap.addLayer(layer);
    }
}
