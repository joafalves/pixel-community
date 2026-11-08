package org.pixel.demo.concept.spaceshooter.world;

/**
 * The enemy ships: collision radius, hit points and the points they are worth.
 */
public enum EnemyType {

    /** Small and fast, flies in formations; one shot destroys it. */
    DART(7f, 1, 100),
    /** Gunship: stops at the right of the screen, strafes and fires aimed bursts. */
    RAIDER(12f, 10, 500),
    /** The boss of every fifth wave (the raider hull, three times larger). */
    DREADNOUGHT(31f, 520, 10_000);

    public final float radius;
    public final int hitPoints;
    public final int points;

    EnemyType(float radius, int hitPoints, int points) {
        this.radius = radius;
        this.hitPoints = hitPoints;
        this.points = points;
    }
}
