/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.physics.handler;

import org.pixel.physics.CollisionGroup;
import org.pixel.math.Vector2;
import org.pixel.physics.Body;
import org.pixel.physics.shape.CircleShape;

import java.util.ArrayList;
import java.util.List;

public class CircleCollisionHandler implements CollisionHandler {

    @Override
    public boolean canHandle(CollisionGroup group) {
        // are the shape of both bodies a circle?
        return group.getA().getShape() instanceof CircleShape &&
                group.getB().getShape() instanceof CircleShape;
    }

    @Override
    public void handleCollision(CollisionGroup group) {
        // extract both bodies and circle shapes
        Body bodyA = group.getA();
        Body bodyB = group.getB();
        CircleShape shapeA = (CircleShape) group.getA().getShape();
        CircleShape shapeB = (CircleShape) group.getB().getShape();

        // calculate the translational vector (normal -> b.pos - a.pos)
        Vector2 normal = Vector2.subtract(bodyB.getPosition(), bodyA.getPosition());

        // collision validation parameters
        float lenSquared = normal.lengthSquared();
        float radius = shapeA.getRadius() + shapeB.getRadius();

        // in contact?
        if (lenSquared >= radius * radius) {
            // not in contact!
            group.setContactPoints(null);
            return;
        }

        // let's calculate the contact point and penetration
        List<Vector2> contactPoints = new ArrayList<>();
        float distance = (float) StrictMath.sqrt(lenSquared);

        if (distance == .0f) {  // are they exactly overlapping?
            group.setNormal(new Vector2(1.0f, 0.0f));
            group.setPenetration(shapeA.getRadius()); // any of them would do
            contactPoints.add(bodyA.getPosition());

        } else {
            normal.divide(distance, distance);
            group.setNormal(normal);
            group.setPenetration(radius - distance);

            Vector2 contactPoint = new Vector2(normal);
            contactPoint.multiply(shapeA.getRadius(), shapeA.getRadius());
            contactPoint.add(bodyA.getPosition());

            contactPoints.add(contactPoint);
        }

        group.setContactPoints(contactPoints);
    }
}
