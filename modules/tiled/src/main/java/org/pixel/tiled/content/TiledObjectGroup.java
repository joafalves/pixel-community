package org.pixel.tiled.content;

import java.util.LinkedHashMap;

public class TiledObjectGroup {
    private LinkedHashMap<Integer, TiledObject> objects;
    private double offsetX;
    private double offsetY;
    private String drawOrder;
    private TiledCustomProperties customProperties;

    public TiledCustomProperties getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(TiledCustomProperties customProperties) {
        this.customProperties = customProperties;
    }

    public double getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(double offsetX) {
        this.offsetX = offsetX;
    }

    public double getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(double offsetY) {
        this.offsetY = offsetY;
    }

    public String getDrawOrder() {
        return drawOrder;
    }

    public void setDrawOrder(String drawOrder) {
        this.drawOrder = drawOrder;
    }

    public LinkedHashMap<Integer, TiledObject> getObjects() {
        return objects;
    }

    public void setObjects(LinkedHashMap<Integer, TiledObject> objects) {
        this.objects = objects;
    }
}
