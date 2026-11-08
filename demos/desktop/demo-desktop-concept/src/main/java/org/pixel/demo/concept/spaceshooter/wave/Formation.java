package org.pixel.demo.concept.spaceshooter.wave;

/**
 * A group of enemies that arrives together (see {@link WaveDirector#build}).
 */
public enum Formation {

    /** Five darts in single file at one height. */
    LINE(true),
    /** Six darts following one sine wave. */
    SINE(true),
    /** Five darts in a V, arriving together. */
    VEE(true),
    /** Five darts dipping in from the top or the bottom. */
    SWOOP(true),
    /** Four darts that turn toward the player. */
    DIVE(true),
    /** One raider gunship. */
    RAIDER(false),
    /** Two raiders, one high and one low. */
    RAIDER_PAIR(false),
    /** The Dreadnought. */
    BOSS(false);

    /** Destroying every ship of the formation drops a capsule. */
    public final boolean rewarded;

    Formation(boolean rewarded) {
        this.rewarded = rewarded;
    }
}
