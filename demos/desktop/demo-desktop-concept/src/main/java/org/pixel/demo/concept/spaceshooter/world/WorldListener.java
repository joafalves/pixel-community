package org.pixel.demo.concept.spaceshooter.world;

/**
 * What happens in the world, for the presentation (effects, popups, shake) and for tests. The simulation never draws:
 * everything visible beyond the entities themselves reacts to these calls. Entities passed in are pooled: read what
 * you need during the call, don't keep them.
 */
public interface WorldListener {

    /** A ship fired a shot from (x, y). */
    default void shotFired(float x, float y, boolean byPlayer) {
    }

    /** A player shot hit an enemy that survived, at (x, y). */
    default void enemyHit(Enemy enemy, float x, float y) {
    }

    /** An enemy was destroyed; {@code points} already include the multiplier. */
    default void enemyDestroyed(Enemy enemy, int points, int multiplier) {
    }

    /** A boss is breaking apart: one blast at (x, y) on its hull. */
    default void bossBlast(Enemy boss, float x, float y) {
    }

    /** A boss finished breaking apart (its last, big explosion). */
    default void bossDefeated(Enemy boss, int points) {
    }

    /** A boss entered a new attack phase (2 or 3). */
    default void bossPhase(Enemy boss, int phase) {
    }

    /** Every ship of a formation was destroyed. */
    default void formationCleared(float x, float y, int points) {
    }

    default void playerHit(Player player) {
    }

    default void playerDestroyed(Player player) {
    }

    default void playerSpawned(Player player) {
    }

    default void extraLife(Player player) {
    }

    /** The player set off a bomb at (x, y). */
    default void bomb(float x, float y) {
    }

    /** An enemy shot at (x, y) was cancelled (by a bomb or a boss's end). */
    default void shotCancelled(float x, float y) {
    }

    default void pickupCollected(PickupType type, float x, float y, int points) {
    }

    default void waveStarted(int wave, boolean boss) {
    }

    default void gameOver(int score) {
    }
}
