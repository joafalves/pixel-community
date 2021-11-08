package org.pixel.tiled.content.importer;

import org.pixel.commons.Pair;
import org.pixel.content.ImportContext;
import org.pixel.tiled.content.Layer;
import org.pixel.tiled.content.TileMap;
import org.pixel.tiled.content.TiledObject;
import org.pixel.tiled.content.TiledObjectGroup;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ObjectGroupCollector extends LayerCollector {
    private final ObjectCollector objectCollector;

    public ObjectGroupCollector() {
        objectCollector = new ObjectCollector();
    }

    public ObjectGroupCollector(ObjectCollector objectCollector) {
        this.objectCollector = objectCollector;
    }

    public Layer collect(TileMap tileMap, Element objectGroupElement, ImportContext ctx) {
        ObjectOrderStrategyFactory factory = new ObjectOrderStrategyFactory();

        List<Pair<Integer, TiledObject>> objects = new ArrayList<>();
        TiledObjectGroup objectGroup = new TiledObjectGroup(collectLayerData(tileMap, objectGroupElement));

        String objectOrder = objectGroupElement.getAttribute("draworder");

        NodeList objectList = objectGroupElement.getElementsByTagName("object");

        for (int j = 0; j < objectList.getLength(); j++) {
            Element objectElement = (Element) objectList.item(j);

            objects.add(objectCollector.collect(objectElement));
        }

        LinkedHashMap<Integer, TiledObject> objectMap = factory.getObjectOrderStrategy(objectOrder).getMap(objects);

        objectGroup.setObjects(objectMap);

        return objectGroup;
    }
}
