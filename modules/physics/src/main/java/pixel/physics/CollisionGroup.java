/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.physics;

import pixel.math.MathHelper;
import pixel.math.Vector2;

import java.util.List;

public class CollisionGroup {

    private final Body a;
    private final Body b;
    private List<Vector2> contactPoints;
    private Vector2 normal;
    private float penetration;
    private float staticFriction;
    private float dynamicFriction;
    private float restitution;

    /**
     * Constructor
     *
     * @param a
     * @param b
     */
    public CollisionGroup(Body a, Body b) {
        this.a = a;
        this.b = b;
        this.penetration = .0f;
    }

    /**
     * Prepare collision values so they can be applied multiple times on impulse function (cache)
     */
    public void solve() {
        restitution = MathHelper.min(a.getRestitution(), b.getRestitution());
        staticFriction = (float) StrictMath.sqrt(a.getStaticFriction() * a.getStaticFriction() +
                b.getStaticFriction() * b.getStaticFriction());
        dynamicFriction = (float) StrictMath.sqrt(a.getDynamicFriction() * a.getDynamicFriction() +
                b.getDynamicFriction() * b.getDynamicFriction());

        /*for (Vector2 contact : contactPoints) {
            Vector2 ra = Vector2.subtract(contact, a.getPosition());
            Vector2 rb = Vector2.subtract(contact, b.getPosition());
            Vector2 rv = Vector2.add(b.getVelocity(), Vector2.cross(rb, b.getAngularVelocity()));
            rv.subtract(a.getVelocity());
            rv.subtract(Vector2.cross(ra, a.getAngularVelocity()));

            // RESTING = GRAVITY.mul( DT ).lengthSq() + EPSILON;
            if (rv.lengthSquared() < 500)
			{
				restitution = 0.0f;
			}
        }*/
    }

    public Body getA() {
        return a;
    }

    public Body getB() {
        return b;
    }

    public boolean hasContactPoints() {
        return this.contactPoints != null && this.contactPoints.size() > 0;
    }

    public List<Vector2> getContactPoints() {
        return contactPoints;
    }

    public void setContactPoints(List<Vector2> contactPoints) {
        this.contactPoints = contactPoints;
    }

    public float getPenetration() {
        return penetration;
    }

    public void setPenetration(float penetration) {
        this.penetration = penetration;
    }

    public Vector2 getNormal() {
        return normal;
    }

    public void setNormal(Vector2 normal) {
        this.normal = normal;
    }

    public float getRestitution() {
        return restitution;
    }

    public float getDynamicFriction() {
        return dynamicFriction;
    }

    public float getStaticFriction() {
        return staticFriction;
    }
}
