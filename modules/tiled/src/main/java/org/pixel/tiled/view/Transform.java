package org.pixel.tiled.view;

class Transform {
    private float scaleX;
    private float scaleY;
    private float rotation;

    Transform() {
        this.scaleX = this.scaleY = 1f;
        this.rotation = 0f;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public float getScaleY() {
        return scaleY;
    }

    public void setScaleY(float scaleY) {
        this.scaleY = scaleY;
    }

    public float getScaleX() {
        return scaleX;
    }

    public void setScaleX(float scaleX) {
        this.scaleX = scaleX;
    }
}
