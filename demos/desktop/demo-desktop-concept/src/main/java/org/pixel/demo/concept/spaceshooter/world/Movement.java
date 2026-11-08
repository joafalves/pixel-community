package org.pixel.demo.concept.spaceshooter.world;

/**
 * How an enemy flies (see {@link EnemyAi}).
 */
public enum Movement {

    /** Straight to the left. */
    STRAIGHT,
    /** To the left on a sine wave around its spawn height. */
    SINE,
    /** In, then turns toward the player's height and speeds up. */
    DIVE,
    /** Dips into the screen from the top or the bottom and leaves the same way. */
    SWOOP,
    /** Flies to a position on the right, strafes there while firing, then leaves. */
    HOLD,
    /** The boss: enters, then strafes with phase-dependent attacks until destroyed. */
    BOSS
}
