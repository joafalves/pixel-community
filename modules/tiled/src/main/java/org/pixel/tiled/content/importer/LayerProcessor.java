package org.pixel.tiled.content.importer;

import org.pixel.content.ImportContext;
import org.pixel.tiled.content.TileMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class LayerProcessor implements TileMapProcessor {
    private final LayerCollectorFactory factory;

    public LayerProcessor() {
        this.factory = new LayerCollectorFactory();
    }

    public LayerProcessor(LayerCollectorFactory factory) {
        this.factory = factory;
    }

    @Override
    public void process(TileMap tileMap, Document document, ImportContext ctx) {
        Element mapElement = document.getDocumentElement();

        NodeList mapChildren = mapElement.getChildNodes();

        for (int i = 0; i < mapChildren.getLength(); i++) {
            Node child = mapChildren.item(i);

            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) child;

                LayerCollector collector = factory.getLayerCollector(child.getNodeName());

                collector.process(tileMap, childElement);
            }
        }
    }
}
