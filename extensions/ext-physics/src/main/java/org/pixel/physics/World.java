/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.physics;

import org.pixel.commons.lifecycle.Clearable;
import org.pixel.commons.lifecycle.Updatable;
import org.pixel.commons.DeltaTime;
import org.pixel.math.Vector2;

import java.util.ArrayList;
import java.util.List;

public class World implements Updatable, Clearable {

    private final CollisionManager collisionManager;
    private final Vector2 gravity;
    private final List<Body> bodyList;

    private List<CollisionGroup> stepCollisions;
    private int iterations;

    /**
     * Constructor
     */
    public World() {
        this.collisionManager = new CollisionManager();
        this.bodyList = new ArrayList<>();
        this.gravity = new Vector2(0, 10.f);
        this.iterations = 10;
    }

    /**
     * Update the physic world
     *
     * @param delta
     */
    @Override
    public void update(DeltaTime delta) {
        // test/solve collisions:
        stepCollisions = collisionManager.detectCollisions(bodyList);

        // apply integrated forces to bodies
        bodyList.iterator().forEachRemaining(body -> applyIntegratedForces(body, delta));

        // process and apply forces to detected collisions
        if (stepCollisions != null && !stepCollisions.isEmpty()) {
            collisionManager.processCollisions(stepCollisions, iterations);
        }

        // apply velocity
        bodyList.iterator().forEachRemaining(body -> applyVelocity(body, delta.getElapsed()));

        endUpdate();
    }

    /**
     * end update cycle
     */
    private void endUpdate() {
        for (Body body : bodyList) {
            body.getForce().set(0);
            body.setTorque(0);
        }
    }

    /**
     * @param body
     * @param delta
     */
    private void applyVelocity(Body body, float delta) {
        if (body.getType() != BodyType.DYNAMIC) {
            return; // nothing to do, no mass..
        }

        body.getPosition().add(body.getVelocity().getX() * delta, body.getVelocity().getY() * delta);
        body.setOrientation(body.getOrientation() + body.getAngularVelocity() * delta);
    }

    /**
     * @param body
     * @param delta
     */
    private void applyIntegratedForces(Body body, DeltaTime delta) {
        if (body.getType() != BodyType.DYNAMIC) {
            return; // nothing to do, no mass..
        }

        body.getVelocity().add(body.getForce().getX() * body.getMass() * delta.getElapsed(),
                body.getForce().getY() * body.getMass() * delta.getElapsed());
        body.getVelocity().add(gravity.getX() * body.getMass() * delta.getElapsed(),
                gravity.getY() * body.getMass() * delta.getElapsed());
        body.setAngularVelocity(body.getAngularVelocity() + body.getTorque() * body.getInertia() * delta.getElapsed());
    }

    /**
     * Add body
     *
     * @param body
     */
    public void addBody(Body body) {
        this.bodyList.add(body);
    }

    /**
     * Remove body
     *
     * @param body
     * @return
     */
    public boolean removeBody(Body body) {
        return this.bodyList.remove(body);
    }

    /**
     * Get a list of current bodies on this world
     *
     * @return
     */
    public List<Body> getBodies() {
        return this.bodyList;
    }

    /**
     * Clear the world
     */
    @Override
    public void clear() {
        this.bodyList.clear();
    }

    public Vector2 getGravity() {
        return gravity;
    }

    public void setGravity(float x, float y) {
        this.gravity.set(x, y);
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public List<CollisionGroup> getStepCollisions() {
        return stepCollisions;
    }
}
