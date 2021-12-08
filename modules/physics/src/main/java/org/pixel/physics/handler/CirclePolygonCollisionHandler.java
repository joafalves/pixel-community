/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.physics.handler;

import org.pixel.physics.Body;
import org.pixel.physics.CollisionGroup;
import org.pixel.physics.shape.CircleShape;
import org.pixel.physics.shape.PolygonShape;

public class CirclePolygonCollisionHandler implements CollisionHandler {

    @Override
    public boolean canHandle(CollisionGroup group) {
        // are the shape of both bodies a circle?
        return (group.getA().getShape() instanceof CircleShape && group.getB().getShape() instanceof PolygonShape) ||
                (group.getA().getShape() instanceof PolygonShape && group.getB().getShape() instanceof CircleShape);
    }

    @Override
    public void handleCollision(CollisionGroup group) {
        // for simplicity’s sake, we are going to consider that 'A' is always circle and 'B' is always polygon
        Body bodyA = group.getA().getShape() instanceof CircleShape ? group.getA() : group.getB();
        Body bodyB = group.getA().getShape() instanceof CircleShape ? group.getB() : group.getA();
        CircleShape shapeA = (CircleShape) (bodyA.getShape() instanceof CircleShape ? bodyA.getShape() : bodyB.getShape());
        PolygonShape shapeB = (PolygonShape) (bodyA.getShape() instanceof CircleShape ? bodyB.getShape() : bodyA.getShape());

    }
}
