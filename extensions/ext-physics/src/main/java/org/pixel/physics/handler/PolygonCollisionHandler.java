/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.physics.handler;

import org.pixel.physics.CollisionGroup;
import org.pixel.physics.shape.PolygonShape;

public class PolygonCollisionHandler implements CollisionHandler {

    @Override
    public boolean canHandle(CollisionGroup group) {
        // are the shape of both bodies a circle?
        return group.getA().getShape() instanceof PolygonShape &&
                group.getB().getShape() instanceof PolygonShape;
    }

    @Override
    public void handleCollision(CollisionGroup group) {
        // todo: do this
    }
}
