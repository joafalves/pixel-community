/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.concept.common;

import org.pixel.math.Vector2;

public class Transform {
    private final Vector2 position;
    private final Vector2 scale;
    private float rotation;

    public Transform() {
        this.position = Vector2.zero();
        this.scale = Vector2.one();
        this.rotation = 0.f;
    }

    public void setPosition(float x, float y) {
        this.position.set(x, y);
    }

    public void translate(float x, float y) {
        this.position.add(x, y);
    }

    public Vector2 getPosition() {
        return this.position;
    }

    public void setScale(float x, float y) {
        this.scale.set(x, y);
    }

    public Vector2 getScale() {
        return this.scale;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public float getRotation() {
        return this.rotation;
    }
}
