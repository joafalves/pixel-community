package org.pixel.ext.tiled.content;

public enum TiledConstants {
    PIXEL_EPSILON(0.005f);
    float value;

    TiledConstants(float value) {
        this.value = value;
    }

    public float getValue() {
        return value;
    }
}
