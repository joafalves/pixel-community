package org.pixel.tiled.content.importer;

import org.pixel.commons.Pair;
import org.pixel.content.ImportContext;
import org.pixel.math.Vector2;
import org.pixel.tiled.content.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ObjectGroupProcessor implements TileMapProcessor {
    @Override
    public void process(TileMap tileMap, Document document, ImportContext ctx) {
        NodeList objectGroupList = document.getElementsByTagName("objectgroup");
        CustomPropertiesCollector collector = new CustomPropertiesCollector();
        List<TiledObjectGroup> result = new ArrayList<>();
        ObjectOrderStrategyFactory factory = new ObjectOrderStrategyFactory();

        for (int i = 0; i < objectGroupList.getLength(); i++) {
            Element objectGroupElement = (Element) objectGroupList.item(i);
            TiledCustomProperties customProperties = collector.collect(objectGroupElement);
            List<Pair<Integer, TiledObject>> objects = new ArrayList<>();
            TiledObjectGroup objectGroup = new TiledObjectGroup();

            String objectOrder = objectGroupElement.getAttribute("draworder");

            try {
                objectGroup.setOffsetX(Float.parseFloat(objectGroupElement.getAttribute("offsetx")));
            } catch (NumberFormatException e) {
                objectGroup.setOffsetX(0);
            }

            try {
                objectGroup.setOffsetY(Float.parseFloat(objectGroupElement.getAttribute("offsety")));
            } catch (NumberFormatException e) {
                objectGroup.setOffsetY(0);
            }
            objectGroup.setCustomProperties(customProperties);

            NodeList objectList = objectGroupElement.getElementsByTagName("object");

            for (int j = 0; j < objectList.getLength(); j++) {
                Element objectElement = (Element) objectList.item(j);
                customProperties = collector.collect(objectElement);
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
                    object.setRotation(Float.parseFloat(objectElement.getAttribute("rotation")));
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
            result.add(objectGroup);
        }

        tileMap.setObjectGroups(result);
    }
}
