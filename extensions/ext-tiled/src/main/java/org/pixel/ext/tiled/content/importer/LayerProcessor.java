package org.pixel.ext.tiled.content.importer;

import org.pixel.content.ImportContext;
import org.pixel.ext.tiled.content.TiledLayer;
import org.pixel.ext.tiled.content.TiledMap;
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

    public List<TiledLayer> processChildren(TiledMap tileMap, Element element, ImportContext ctx) {
        NodeList mapChildren = element.getChildNodes();
        List<TiledLayer> layers = new ArrayList<>();

        for (int i = 0; i < mapChildren.getLength(); i++) {
            Node child = mapChildren.item(i);

            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) child;

                LayerCollector collector = factory.getLayerCollector(child.getNodeName());

                TiledLayer layer = collector.collect(tileMap, childElement, ctx);

                if (layer != null) {
                    layers.add(layer);
                }
            }
        }

        return layers;
    }

    @Override
    public void process(TiledMap tileMap, Document document, ImportContext ctx) {
        Element mapElement = document.getDocumentElement();

        tileMap.setLayers(processChildren(tileMap, mapElement, ctx));
    }
}
