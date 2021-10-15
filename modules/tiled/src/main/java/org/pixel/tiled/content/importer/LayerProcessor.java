package org.pixel.tiled.content.importer;

import org.pixel.content.ImportContext;
import org.pixel.tiled.content.Layer;
import org.pixel.tiled.content.TileMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Arrays;
import java.util.List;

public class LayerProcessor implements TileMapProcessor{
    @Override
    public void process(TileMap tileMap, Document tmxDoc, ImportContext ctx) {
        NodeList layers = tmxDoc.getElementsByTagName("layer");

        for(int i = 0; i < layers.getLength(); i++) {
            Node layerNode = layers.item(i);

            if(layerNode.getNodeType() == Node.ELEMENT_NODE) {
                Element layerElement = (Element) layerNode;

                int width = Integer.parseInt(layerElement.getAttribute("width"));
                int height = Integer.parseInt(layerElement.getAttribute("height"));
                double offsetX, offsetY;
                try {
                    offsetX = Double.parseDouble(layerElement.getAttribute("offsetx"));
                } catch (NumberFormatException e) {
                    offsetX = 0;
                }

                try {
                    offsetY = Double.parseDouble(layerElement.getAttribute("offsety"));
                } catch (NumberFormatException e) {
                    offsetY = 0;
                }

                Layer layer = new Layer(width, height, offsetX, offsetY, tileMap);

                String data = layerElement.getTextContent();
                data = data.replace("\n", "");

                List<String> numbers = Arrays.asList(data.trim().split(","));

                for(int y = 0; y < height; y++) {
                    for(int x = 0; x < width; x++) {
                        layer.addTile(x, y, Long.parseLong(numbers.get(y*width+x)));
                    }
                }

                tileMap.addLayer(layer);
            }
        }
    }
}
