package org.pixel.tiled.content.importer;

import org.pixel.content.ImportContext;
import org.pixel.tiled.content.Layer;
import org.pixel.tiled.content.TileMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class LayerProcessor implements TileMapProcessor {
    private final LayerCollectorFactory factory;

    public LayerProcessor() {
        this.factory = new LayerCollectorFactory();
    }

    public LayerProcessor(LayerCollectorFactory factory) {
        this.factory = factory;
    }

    public List<Layer> processChildren(TileMap tileMap, Element element) {
        NodeList mapChildren = element.getChildNodes();
        List<Layer> layers = new ArrayList<>();

        for (int i = 0; i < mapChildren.getLength(); i++) {
            Node child = mapChildren.item(i);

            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) child;

                LayerCollector collector = factory.getLayerCollector(child.getNodeName());

                Layer layer = collector.process(tileMap, childElement);

                if (layer != null) {
                    layers.add(layer);
                }
            }
        }

        return layers;
    }

    @Override
    public void process(TileMap tileMap, Document document, ImportContext ctx) {
        Element mapElement = document.getDocumentElement();

        tileMap.setLayers(processChildren(tileMap, mapElement));
    }
}
