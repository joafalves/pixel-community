package org.pixel.demo.concept.spaceshooter.world;

/**
 * What a pickup gives (or points, when it can't: weapon at full power, bombs or hull full).
 */
public enum PickupType {

    /** One more weapon level. */
    POWER("P", 2_000),
    /** One more bomb. */
    BOMB("B", 1_500),
    /** Repairs one hull point. */
    REPAIR("+", 1_000);

    /** The letter on the capsule. */
    public final String letter;
    /** Points given instead when the pickup has nothing left to give. */
    public final int pointsWhenFull;

    PickupType(String letter, int pointsWhenFull) {
        this.letter = letter;
        this.pointsWhenFull = pointsWhenFull;
    }
}
