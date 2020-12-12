/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.physics;

import pixel.math.Vector2;
import pixel.physics.handler.CircleCollisionHandler;
import pixel.physics.handler.CirclePolygonCollisionHandler;
import pixel.physics.handler.CollisionHandler;
import pixel.physics.handler.PolygonCollisionHandler;

import java.util.ArrayList;
import java.util.List;

public class CollisionManager {

    private static final float PENETRATION_ALLOWED = 0.05f;

    private List<CollisionGroup> collisions;
    private List<CollisionHandler> collisionHandlers;

    /**
     * Constructor
     */
    public CollisionManager() {
        this.collisions = new ArrayList<>();
        this.collisionHandlers = new ArrayList<>();
        this.collisionHandlers.add(new CircleCollisionHandler());
        this.collisionHandlers.add(new CirclePolygonCollisionHandler());
        this.collisionHandlers.add(new PolygonCollisionHandler());
    }

    /**
     * Detect collisions
     *
     * @param bodyList
     * @return
     */
    public List<CollisionGroup> detectCollisions(List<Body> bodyList) {
        collisions.clear();

        // all objects are tested which means that a contact resolution MUST happen for all existing bodies
        for (int i = 0; i < bodyList.size(); ++i) {
            Body a = bodyList.get(i);
            for (int j = i + 1; j < bodyList.size(); ++j) {
                Body b = bodyList.get(j);

                if (a.getMass() == 0 && b.getMass() == 0) {
                    continue; // nothing else to do..
                }

                CollisionGroup group = new CollisionGroup(a, b);
                if (validateCollision(group) && group.hasContactPoints()) {
                    // collision detected!
                    collisions.add(group);
                }
            }
        }

        return collisions;
    }

    /**
     * Process and apply forces to a given group of collisions
     *
     * @param collisions
     * @param iterations
     */
    public void processCollisions(List<CollisionGroup> collisions, int iterations) {
        // solve/cache existing collision groups:
        for (CollisionGroup group : collisions) {
            group.solve();
        }

        // apply impulses
        for (int i = 0; i < iterations; i++) {
            for (CollisionGroup group : collisions) {
                applyImpulse(group);
            }
        }

        // correct positions
        for (CollisionGroup group : collisions) {
            correctPosition(group);
        }
    }

    private void correctPosition(CollisionGroup group) {
        Body a = group.getA();
        Body b = group.getB();
        if (a.getType() == BodyType.STATIC && b.getType() == BodyType.STATIC) {
            return; // no correction if both are static!
        }

        float correction = StrictMath.max(group.getPenetration() - PENETRATION_ALLOWED, 0.0f);
        if (correction <= 0) {
            return;
        }

        if (!a.getType().equals(BodyType.STATIC) && b.getType().equals(BodyType.STATIC)) {
            correction *= (1.0f / a.getMass());
            a.getPosition().add(-group.getNormal().getX() * correction, -group.getNormal().getY() * correction);

        } else {
            correction *= (1.0f / b.getMass());
            b.getPosition().add(group.getNormal().getX() * correction, group.getNormal().getY() * correction);
        }
    }

    /**
     * @param group
     */
    public void applyImpulse(CollisionGroup group) {
        Body a = group.getA();
        Body b = group.getB();
        if (a.getType() != BodyType.DYNAMIC && b.getType() != BodyType.DYNAMIC) {
            return; // none will have physical manifestations
        }

        int contactPoints = group.getContactPoints().size();
        for (Vector2 contact : group.getContactPoints()) {
            // relative orientation:
            Vector2 ra = Vector2.subtract(contact, a.getPosition());
            Vector2 rb = Vector2.subtract(contact, b.getPosition());

            // relative velocity:
            Vector2 rv = Vector2.add(b.getVelocity(), Vector2.cross(rb, b.getAngularVelocity()));
            rv.subtract(a.getVelocity());
            rv.subtract(Vector2.cross(ra, a.getAngularVelocity()));

            // relative velocity along the normal:
            float nv = Vector2.dot(rv, group.getNormal());
            // do nothing else if the velocities are 'separating'
            if (nv > 0) {
                return;
            }

            float raCrossN = Vector2.cross(ra, group.getNormal());
            float rbCrossN = Vector2.cross(rb, group.getNormal());
            float massSum = a.getMass() + b.getMass() + (raCrossN * raCrossN) * a.getInertia() +
                    (rbCrossN * rbCrossN) * b.getInertia();

            // impulse scalar
            float sc = -(1.f + group.getRestitution()) * nv;
            sc /= massSum;
            sc /= contactPoints;

            // apply impulse:
            Vector2 impulse = new Vector2(group.getNormal());
            impulse.multiply(sc, sc);

            if (a.getType() == BodyType.DYNAMIC) {
                a.applyImpulse(new Vector2(-impulse.getX(), -impulse.getY()), ra);
            }

            if (b.getType() == BodyType.DYNAMIC) {
                b.applyImpulse(impulse, rb);
            }

            // friction impulse
            float d = -Vector2.dot(rv, group.getNormal());
            Vector2 t = new Vector2(rv);
            t.add(group.getNormal().getX() * d, group.getNormal().getY() * d);
            t.normalize();

            // tangent
            float tng = -Vector2.dot(rv, t);
            tng /= massSum;
            tng /= contactPoints;

            // TODO: check tiny fraction impulses (and ignore)

            // coulumb's law:
            Vector2 tngImpulse = t;
            if (StrictMath.abs(tng) < sc * group.getStaticFriction()) {
                tngImpulse.multiply(tng, tng);

            } else {
                tngImpulse.multiply(sc, sc);
                tngImpulse.multiply(-group.getDynamicFriction(), -group.getDynamicFriction());
            }

            if (a.getType() == BodyType.DYNAMIC) {
                a.applyImpulse(new Vector2(-tngImpulse.getX(), -tngImpulse.getY()), ra);
            }

            if (b.getType() == BodyType.DYNAMIC) {
                b.applyImpulse(tngImpulse, rb);
            }
        }
    }

    /**
     * Process given collision group using appropriate handlers
     *
     * @param group
     */
    private boolean validateCollision(CollisionGroup group) {
        for (CollisionHandler collisionHandler : collisionHandlers) {
            if (collisionHandler.canHandle(group)) {
                collisionHandler.handleCollision(group);
                return true;
            }
        }

        return false;
    }

    public void addCollisionHandler(CollisionHandler handler) {
        this.collisionHandlers.add(handler);
    }

    public boolean removeCollisionHandler(CollisionHandler handler) {
        return this.collisionHandlers.remove(handler);
    }
}
