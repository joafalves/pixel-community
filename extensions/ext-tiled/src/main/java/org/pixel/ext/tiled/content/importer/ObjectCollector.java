package org.pixel.ext.tiled.content.importer;

import org.pixel.commons.data.Pair;
import org.pixel.ext.tiled.content.*;
import org.pixel.math.MathHelper;
import org.pixel.math.Vector2;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ObjectCollector {
    public Pair<Integer, TiledObject> collect(Element objectElement) {
        CustomPropertiesCollector collector = new CustomPropertiesCollector();

        TiledCustomProperties customProperties = collector.collect(objectElement);
        TiledObject object;

        long gID;

        double width, height;

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

        NodeList list;

        if (!Objects.equals(objectElement.getAttribute("gid"), "")) {
            gID = Long.parseLong(objectElement.getAttribute("gid"));

            TiledTileObject tile = new TiledTileObject();

            tile.setgID(gID);

            tile.setWidth(width);
            tile.setHeight(height);

            object = tile;
        } else if (objectElement.getElementsByTagName("ellipse").getLength() > 0) {
            TiledEllipse ellipse = new TiledEllipse();

            ellipse.setHeight(height);
            ellipse.setWidth(width);

            object = ellipse;
        } else if (objectElement.getElementsByTagName("point").getLength() > 0) {
            object = new TiledPoint();
        } else if ((list = objectElement.getElementsByTagName("polygon")).getLength() > 0) {
            Element polygonElement = (Element) list.item(0);

            List<String> points = Arrays.asList(polygonElement.getAttribute("points").trim().split(" "));
            List<Vector2> vertices = new ArrayList<>();

            points.forEach(s -> {
                List<String> values = Arrays.asList(s.split(","));

                vertices.add(new Vector2(Float.parseFloat(values.get(0)), Float.parseFloat(values.get(1))));
            });

            TiledPolygon polygon = new TiledPolygon();

            polygon.setVertices(vertices);

            object = polygon;
        } else {
            TiledRectangle rectangle = new TiledRectangle();

            rectangle.setHeight(height);
            rectangle.setWidth(width);

            object = rectangle;
        }

        try {
            object.setRotation(MathHelper.toRadians(Float.parseFloat(objectElement.getAttribute("rotation"))));
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

        object.setCustomProperties(customProperties);

        return new Pair<>(Integer.parseInt(objectElement.getAttribute("id")), object);
    }
}
