package org.pixel.ext.tiled.content.importer;

import org.pixel.content.ImportContext;
import org.pixel.ext.tiled.content.TiledLayer;
import org.pixel.ext.tiled.content.TiledMap;
import org.pixel.ext.tiled.content.TiledTileLayer;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Arrays;
import java.util.List;

class TileLayerCollector extends LayerCollector {
    @Override
    public TiledLayer collect(TiledMap tileMap, Element tileLayerElement, ImportContext ctx) {
        int width = Integer.parseInt(tileLayerElement.getAttribute("width"));
        int height = Integer.parseInt(tileLayerElement.getAttribute("height"));

        TiledTileLayer layer = new TiledTileLayer(collectLayerData(tileMap, tileLayerElement), width, height);

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

        return layer;
    }
}
