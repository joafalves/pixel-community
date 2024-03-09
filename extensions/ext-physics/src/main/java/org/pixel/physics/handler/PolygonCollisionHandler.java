/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.physics.handler;

import org.pixel.math.Vector2;
import org.pixel.physics.CollisionGroup;
import org.pixel.physics.shape.PolygonShape;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class PolygonCollisionHandler implements CollisionHandler {

    @Override
    public boolean canHandle(CollisionGroup group) {
        return group.getA().getShape() instanceof PolygonShape &&
                group.getB().getShape() instanceof PolygonShape;
    }

    @Override
    public void handleCollision(CollisionGroup group) {
        var bodyA = group.getA();
        var bodyB = group.getB();
        PolygonShape shapeA = (PolygonShape) bodyA.getShape();
        PolygonShape shapeB = (PolygonShape) bodyB.getShape();

        Vector2 mtv = findMTV(shapeA, shapeB);
        if (mtv != null) {
            // Collision detected
            // Resolve the collision using mtv
            group.setNormal(mtv); // This is a simplified approach
            group.setPenetration(mtv.length()); // You might need a more accurate penetration depth calculation

            // Basic contact point calculation
            List<Vector2> contactPoints = calculateContactPoints(shapeA, shapeB, mtv);
            group.setContactPoints(contactPoints);
        } else {
            // No collision
            group.setContactPoints(null);
        }
    }

    private Vector2 findMTV(PolygonShape shapeA, PolygonShape shapeB) {
        Vector2 smallestAxis = null;
        float smallestOverlap = Float.MAX_VALUE;

        Vector2[] axes = concatenate(shapeA.getAxes(), shapeB.getAxes());
        for (Vector2 axis : axes) {
            Projection proj1 = project(shapeA, axis);
            Projection proj2 = project(shapeB, axis);

            float overlap = getOverlap(proj1, proj2);
            if (overlap == 0) {
                return null; // No collision if there's no overlap on this axis
            }

            if (overlap < smallestOverlap) {
                smallestOverlap = overlap;
                smallestAxis = axis;
            }
        }

        if (smallestAxis == null) {
            return null; // No collision
        }

        // Ensure MTV pushes A away from B
        Vector2 d = Vector2.subtract(shapeB.getCenter(), shapeA.getCenter());
        if (Vector2.dot(d, smallestAxis) < 0) {
            smallestAxis = Vector2.multiply(smallestAxis, -1);
        }

        return Vector2.multiply(smallestAxis, smallestOverlap);
    }

    private Projection project(PolygonShape polygon, Vector2 axis) {
        float min = Vector2.dot(axis, polygon.getVertices()[0]);
        float max = min;
        for (Vector2 v : polygon.getVertices()) {
            float p = Vector2.dot(axis, v);
            if (p < min) {
                min = p;
            } else if (p > max) {
                max = p;
            }
        }
        return new Projection(min, max);
    }

    private float getOverlap(Projection proj1, Projection proj2) {
        if (proj1.max < proj2.min || proj2.max < proj1.min) {
            return 0; // No overlap
        }
        return Math.min(proj1.max, proj2.max) - Math.max(proj1.min, proj2.min);
    }

    private Vector2[] concatenate(Vector2[] a, Vector2[] b) {
        int aLen = a.length;
        int bLen = b.length;
        Vector2[] c = new Vector2[aLen + bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    private List<Vector2> calculateContactPoints(PolygonShape shapeA, PolygonShape shapeB, Vector2 mtv) {
        List<Vector2> contactPoints = new ArrayList<>();

        // Simplified approach: Find vertices of A that are inside B and vice versa
        for (Vector2 vertex : shapeA.getVertices()) {
            if (isInsidePolygon(vertex, shapeB)) {
                contactPoints.add(vertex);
            }
        }
        for (Vector2 vertex : shapeB.getVertices()) {
            if (isInsidePolygon(vertex, shapeA)) {
                contactPoints.add(vertex);
            }
        }

        // If no vertices are inside, optionally find the closest points on edges (more complex)

        return contactPoints;
    }

    private boolean isInsidePolygon(Vector2 point, PolygonShape polygon) {
        int intersectCount = 0;
        for (int i = 0; i < polygon.getVertices().length; i++) {
            Vector2 v1 = polygon.getVertices()[i];
            Vector2 v2 = polygon.getVertices()[(i + 1) % polygon.getVertices().length];

            if (isIntersecting(point, new Vector2(Float.MAX_VALUE, point.getY()), v1, v2)) {
                intersectCount++;
            }
        }
        return intersectCount % 2 == 1;
    }

    private boolean isIntersecting(Vector2 p1, Vector2 p2, Vector2 q1, Vector2 q2) {
        // First check if bounding boxes intersect
        if (Math.max(p1.getX(), p2.getX()) < Math.min(q1.getX(), q2.getX()) ||
                Math.max(q1.getX(), q2.getX()) < Math.min(p1.getX(), p2.getX()) ||
                Math.max(p1.getY(), p2.getY()) < Math.min(q1.getY(), q2.getY()) ||
                Math.max(q1.getY(), q2.getY()) < Math.min(p1.getY(), p2.getY())) {
            return false;
        }

        // Calculate orientation of the triplet (p, q, r)
        // The function returns 0 if p, q and r are collinear,
        // 1 if they are clockwise, and 2 if counter-clockwise
        Function<Vector2, Integer> orientation = (r) -> {
            float val = (q1.getY() - p1.getY()) * (r.getX() - q1.getX()) -
                    (q1.getX() - p1.getX()) * (r.getY() - q1.getY());
            if (val == 0) return 0;  // Colinear
            return (val > 0) ? 1 : 2; // Clockwise or Counterclockwise
        };

        // Check if the line segments intersect
        int o1 = orientation.apply(p1);
        int o2 = orientation.apply(p2);
        int o3 = orientation.apply(q1);
        int o4 = orientation.apply(q2);

        // General case
        if (o1 != o2 && o3 != o4)
            return true;

        // Special cases
        // p1, q1 and p2 are collinear and p2 lies on segment p1q1
        if (o1 == 0 && onSegment(p1, p2, q1)) return true;

        // p1, q1 and q2 are collinear and q2 lies on segment p1q1
        if (o2 == 0 && onSegment(p1, q2, q1)) return true;

        // p2, q2 and p1 are collinear and p1 lies on segment p2q2
        if (o3 == 0 && onSegment(p2, p1, q2)) return true;

        // p2, q2 and q1 are collinear and q1 lies on segment p2q2
        if (o4 == 0 && onSegment(p2, q1, q2)) return true;

        return false; // Doesn't fall in any of the above cases
    }

    private boolean onSegment(Vector2 p, Vector2 q, Vector2 r) {
        return q.getX() <= Math.max(p.getX(), r.getX()) && q.getX() >= Math.min(p.getX(), r.getX()) &&
                q.getY() <= Math.max(p.getY(), r.getY()) && q.getY() >= Math.min(p.getY(), r.getY());
    }


    static class Projection {
        float min, max;

        public Projection(float min, float max) {
            this.min = min;
            this.max = max;
        }
    }
}
