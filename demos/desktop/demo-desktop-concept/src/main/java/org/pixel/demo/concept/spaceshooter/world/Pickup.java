package org.pixel.demo.concept.spaceshooter.world;

/**
 * A capsule dropped by an enemy (pooled).
 */
public final class Pickup {

    public boolean active;
    public PickupType type;
    public float x, y, vx, vy, age;
}
