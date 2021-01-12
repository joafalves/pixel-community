/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.physics.handler;

import org.pixel.physics.Body;
import org.pixel.physics.CollisionGroup;
import org.pixel.physics.shape.Circle;
import org.pixel.physics.shape.Polygon;

public class CirclePolygonCollisionHandler implements CollisionHandler {

    @Override
    public boolean canHandle(CollisionGroup group) {
        // are the shape of both bodies a circle?
        return (group.getA().getShape() instanceof Circle && group.getB().getShape() instanceof Polygon) ||
                (group.getA().getShape() instanceof Polygon && group.getB().getShape() instanceof Circle);
    }

    @Override
    public void handleCollision(CollisionGroup group) {
        // for simplicity sake, we are going to consider that 'A' is always circle and 'B' is always polygon
        Body bodyA = group.getA().getShape() instanceof Circle ? group.getA() : group.getB();
        Body bodyB = group.getA().getShape() instanceof Circle ? group.getB() : group.getA();
        Circle shapeA = (Circle) (bodyA.getShape() instanceof Circle ? bodyA.getShape() : bodyB.getShape());
        Polygon shapeB = (Polygon) (bodyA.getShape() instanceof Circle ? bodyB.getShape() : bodyA.getShape());

        // TODO: do this
    }
}
