/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.concept.common;

import org.pixel.commons.lifecycle.Entity;
import org.pixel.commons.DeltaTime;

import java.util.LinkedList;

public abstract class GameObject implements Entity {

    private final LinkedList<GameComponent> components;
    private final Transform transform;

    public GameObject() {
        this.components = new LinkedList<>();
        this.transform = new Transform();
    }

    @Override
    public void update(DeltaTime delta) {
        for (GameComponent component : components) {
            component.update(delta);
        }
    }

    public void addComponent(GameComponent component) {
        components.add(component);
        component.setParent(this);
    }

    public Transform getTransform() {
        return transform;
    }
}
