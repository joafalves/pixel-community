package org.pixel.tiled.content.importer;

import org.pixel.commons.Pair;
import org.pixel.math.MathHelper;
import org.pixel.math.Vector2;
import org.pixel.tiled.content.DrawableTiledObject;
import org.pixel.tiled.content.TiledCustomProperties;
import org.pixel.tiled.content.TiledObject;
import org.pixel.tiled.content.TiledTileObject;
import org.w3c.dom.Element;

public class ObjectCollector {
    public Pair<Integer, DrawableTiledObject> collect(Element objectElement) {
        CustomPropertiesCollector collector = new CustomPropertiesCollector();

        TiledCustomProperties customProperties = collector.collect(objectElement);
        DrawableTiledObject object;

        long gID;

        try {
            gID = Long.parseLong(objectElement.getAttribute("gid"));

            TiledTileObject tile = new TiledTileObject();

            tile.setgID(gID);
            object = tile;
        } catch (NumberFormatException e) {
            object = new DrawableTiledObject();
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

        Pair<Integer, DrawableTiledObject> pair = new Pair<>(Integer.parseInt(objectElement.getAttribute("id")), object);

        return pair;
    }
}
