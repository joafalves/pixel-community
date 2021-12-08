/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.physics;

import org.pixel.math.Vector2;
import org.pixel.physics.shape.Shape;

import java.io.Serializable;

public class Body implements Serializable, Cloneable {

    private BodyType type;
    private Shape shape;
    private Vector2 position;
    private Vector2 velocity;
    private Vector2 force;
    private Object attachment;
    private float mass;
    private float angularVelocity;
    private float torque;
    private float inertia;
    private float orientation;
    private float restitution;
    private float staticFriction;
    private float dynamicFriction;

    /**
     * Constructor
     *
     * @param position
     */
    public Body(Vector2 position) {
        this.position = position;
        this.velocity = Vector2.zero();
        this.force = Vector2.zero();
        this.mass = 1.0f;
        this.angularVelocity = 0;
        this.torque = 0;
        this.inertia = 0.0f;
        this.orientation = 0;
        this.staticFriction = 0.1f;
        this.dynamicFriction = 0.1f;
        this.restitution = 0.5f;
        this.type = BodyType.DYNAMIC;
    }

    public void resetVelocity() {
        this.velocity.set(0);
        this.angularVelocity = 0;
    }

    /**
     * @param force
     */
    public void applyImpulse(float force) {
        velocity.add(force * (float) Math.cos(orientation), force * (float) Math.sin(orientation));
    }

    /**
     * @param impulse
     * @param vector
     */
    public void applyImpulse(Vector2 impulse, Vector2 vector) {
        velocity.add(impulse.getX() * mass, impulse.getY() * mass);
        angularVelocity += inertia * Vector2.cross(vector, impulse);
    }

    public void setInertia(float inertia) {
        this.inertia = inertia;
    }

    public float getInertia() {
        return inertia;
    }

    public float getMass() {
        return mass;
    }

    public void setMass(float mass) {
        this.mass = mass;
    }

    public Shape getShape() {
        return shape;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 position) {
        this.position = position;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector2 velocity) {
        this.velocity = velocity;
    }

    public Vector2 getForce() {
        return force;
    }

    public void setForce(Vector2 force) {
        this.force = force;
    }

    public float getAngularVelocity() {
        return angularVelocity;
    }

    public void setAngularVelocity(float angularVelocity) {
        this.angularVelocity = angularVelocity;
    }

    public float getTorque() {
        return torque;
    }

    public void setTorque(float torque) {
        this.torque = torque;
    }

    public float getOrientation() {
        return orientation;
    }

    public void setOrientation(float orientation) {
        this.orientation = orientation;
    }

    public float getRestitution() {
        return restitution;
    }

    public void setRestitution(float restitution) {
        this.restitution = restitution;
    }

    public float getDynamicFriction() {
        return dynamicFriction;
    }

    public void setDynamicFriction(float dynamicFriction) {
        this.dynamicFriction = dynamicFriction;
    }

    public float getStaticFriction() {
        return staticFriction;
    }

    public void setStaticFriction(float staticFriction) {
        this.staticFriction = staticFriction;
    }

    public BodyType getType() {
        return type;
    }

    public void setType(BodyType type) {
        this.type = type;
    }

    public Object getAttachment() {
        return attachment;
    }

    public void setAttachment(Object attachment) {
        this.attachment = attachment;
    }
}
