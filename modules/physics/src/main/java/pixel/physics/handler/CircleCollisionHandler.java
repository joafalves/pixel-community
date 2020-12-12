/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.physics.handler;

import pixel.math.Vector2;
import pixel.physics.Body;
import pixel.physics.CollisionGroup;
import pixel.physics.shape.Circle;

import java.util.ArrayList;
import java.util.List;

public class CircleCollisionHandler implements CollisionHandler {

    @Override
    public boolean canHandle(CollisionGroup group) {
        // are the shape of both bodies a circle?
        return group.getA().getShape() instanceof Circle &&
                group.getB().getShape() instanceof Circle;
    }

    @Override
    public void handleCollision(CollisionGroup group) {
        // extract both bodies and circle shapes
        Body bodyA = group.getA();
        Body bodyB = group.getB();
        Circle shapeA = (Circle) group.getA().getShape();
        Circle shapeB = (Circle) group.getB().getShape();

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

        // are they exactly overlapping?
        if (distance == .0f) {
            // yes, this is easy!
            group.setPenetration(shapeA.getRadius()); // any of them would do
            group.setNormal(new Vector2(1.0f, 0.0f));
            contactPoints.add(bodyA.getPosition());

        } else {
            // this will be the scenario to run 99.99% of the times
            group.setPenetration(radius - distance);

            normal.divide(distance, distance);
            group.setNormal(normal);

            Vector2 contactPoint = new Vector2(normal);
            contactPoint.multiply(shapeA.getRadius(), shapeA.getRadius());
            contactPoint.add(bodyA.getPosition());
            contactPoints.add(contactPoint);
        }

        group.setContactPoints(contactPoints);
    }
}
