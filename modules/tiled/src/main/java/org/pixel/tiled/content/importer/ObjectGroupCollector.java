package org.pixel.tiled.content.importer;

import org.pixel.commons.Pair;
import org.pixel.math.MathHelper;
import org.pixel.math.Vector2;
import org.pixel.tiled.content.*;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ObjectGroupCollector extends LayerCollector {
    public Layer process(TileMap tileMap, Element objectGroupElement) {
        CustomPropertiesCollector collector = new CustomPropertiesCollector();
        ObjectOrderStrategyFactory factory = new ObjectOrderStrategyFactory();

        List<Pair<Integer, TiledObject>> objects = new ArrayList<>();
        TiledObjectGroup objectGroup = new TiledObjectGroup(collectLayerData(tileMap, objectGroupElement));

        String objectOrder = objectGroupElement.getAttribute("draworder");

        NodeList objectList = objectGroupElement.getElementsByTagName("object");

        for (int j = 0; j < objectList.getLength(); j++) {
            Element objectElement = (Element) objectList.item(j);
            TiledCustomProperties customProperties = collector.collect(objectElement);
            TiledObject object;

            long gID;

            try {
                gID = Long.parseLong(objectElement.getAttribute("gid"));

                TiledTileObject tile = new TiledTileObject();

                tile.setgID(gID);
                object = tile;
            } catch (NumberFormatException e) {
                object = new TiledObject();
            }

            try {
                object.setRotation(MathHelper.degToRad(Float.parseFloat(objectElement.getAttribute("rotation"))));
            } catch (NumberFormatException e) {
                object.setRotation(0f);
            }

            float x, y;

            try {
                x = Float.parseFloat(objectElement.getAttribute("x"));
            } catch (NumberFormatException e) {
                x = 0;
            }

            try {
                y = Float.parseFloat(objectElement.getAttribute("y"));
            } catch (NumberFormatException e) {
                y = 0;
            }
            object.setPosition(new Vector2(x, y));

            float width, height;

            try {
                width = Float.parseFloat(objectElement.getAttribute("width"));
            } catch (NumberFormatException e) {
                width = 0;
            }

            try {
                height = Float.parseFloat(objectElement.getAttribute("height"));
            } catch (NumberFormatException e) {
                height = 0;
            }

            object.setWidth(width);
            object.setHeight(height);
            object.setCustomProperties(customProperties);

            Pair<Integer, TiledObject> pair = new Pair<>(Integer.parseInt(objectElement.getAttribute("id")), object);
            objects.add(pair);
        }

        LinkedHashMap<Integer, TiledObject> objectMap = factory.getObjectOrderStrategy(objectOrder).getMap(objects);

        objectGroup.setObjects(objectMap);

        return objectGroup;
    }
}
