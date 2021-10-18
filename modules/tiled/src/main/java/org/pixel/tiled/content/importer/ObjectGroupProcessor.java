package org.pixel.tiled.content.importer;

import org.pixel.content.ImportContext;
import org.pixel.math.Vector2;
import org.pixel.tiled.content.TileMap;
import org.pixel.tiled.content.TiledCustomProperties;
import org.pixel.tiled.content.TiledObject;
import org.pixel.tiled.content.TiledObjectGroup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

public class ObjectGroupProcessor implements TileMapProcessor {
    @Override
    public void process(TileMap tileMap, Document document, ImportContext ctx) {
        NodeList objectGroupList = document.getElementsByTagName("objectgroup");
        CustomPropertiesCollector collector = new CustomPropertiesCollector();
        List<TiledObjectGroup> result = new ArrayList<>();

        for (int i = 0; i < objectGroupList.getLength(); i++) {
            Element objectGroupElement = (Element) objectGroupList.item(i);
            TiledCustomProperties customProperties = collector.collect(objectGroupElement);
            LinkedHashMap<Integer, TiledObject> objectMap = new LinkedHashMap<>();
            TiledObjectGroup objectGroup = new TiledObjectGroup();

            objectGroup.setDrawOrder(objectGroupElement.getAttribute("draworder"));

            if (Objects.equals(objectGroup.getDrawOrder(), "")) {
                objectGroup.setDrawOrder("topdown");
            }

            objectGroup.setOffsetX(Float.parseFloat(objectGroupElement.getAttribute("offsetx")));
            objectGroup.setOffsetY(Float.parseFloat(objectGroupElement.getAttribute("offsety")));
            objectGroup.setCustomProperties(customProperties);

            NodeList objectList = objectGroupElement.getElementsByTagName("object");

            for (int j = 0; j < objectList.getLength(); j++) {
                Element objectElement = (Element) objectList.item(j);
                customProperties = collector.collect(objectElement);
                TiledObject object = new TiledObject();

                try {
                    object.setgID(Long.parseLong(objectElement.getAttribute("gid")));
                } catch (NumberFormatException e) {
                    object.setgID(0);
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

                objectMap.put(Integer.parseInt(objectElement.getAttribute("id")), object);
            }

            objectGroup.setObjects(objectMap);
            result.add(objectGroup);
        }

        tileMap.setObjectGroups(result);
    }
}
