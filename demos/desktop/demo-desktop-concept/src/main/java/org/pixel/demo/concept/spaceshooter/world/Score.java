package org.pixel.demo.concept.spaceshooter.world;

import org.pixel.demo.concept.spaceshooter.GameConfig;

/**
 * Points of a run, the kill chain and its multiplier, and extra lives (the session's best score is kept by the
 * game, not here: attract-mode runs don't count).
 * <p>
 * Kills less than {@link GameConfig#CHAIN_WINDOW} seconds apart build a chain; every
 * {@link GameConfig#CHAIN_PER_MULTIPLIER} kills in it raise the multiplier, up to {@link GameConfig#MAX_MULTIPLIER}.
 * Getting hit breaks the chain.
 */
public final class Score {

    private int points, chain;
    private float chainTimer;
    private int nextExtraLife = GameConfig.EXTRA_LIFE_EVERY;

    /** A new run. */
    public void reset() {
        points = 0;
        chain = 0;
        chainTimer = 0f;
        nextExtraLife = GameConfig.EXTRA_LIFE_EVERY;
    }

    /**
     * A kill: extends the chain and adds the points times the multiplier.
     *
     * @return The points awarded.
     */
    public int kill(int basePoints) {
        chain++;
        chainTimer = GameConfig.CHAIN_WINDOW;
        return add(basePoints * multiplier());
    }

    /**
     * Points without the multiplier (pickups, bonuses).
     *
     * @return The points awarded.
     */
    public int add(int amount) {
        points += amount;
        return amount;
    }

    public void update(float dt) {
        if (chain > 0) {
            chainTimer -= dt;
            if (chainTimer <= 0f) {
                breakChain();
            }
        }
    }

    public void breakChain() {
        chain = 0;
        chainTimer = 0f;
    }

    /**
     * @return Whether the score just passed the next extra-life threshold (true once per threshold).
     */
    boolean takeExtraLife() {
        if (points >= nextExtraLife) {
            nextExtraLife += GameConfig.EXTRA_LIFE_EVERY;
            return true;
        }
        return false;
    }

    public int multiplier() {
        return Math.min(GameConfig.MAX_MULTIPLIER, 1 + chain / GameConfig.CHAIN_PER_MULTIPLIER);
    }

    public int points() {
        return points;
    }

    public int chain() {
        return chain;
    }

    /** Time left in the chain window, 1 (just extended) to 0 (broken). */
    public float chainRemaining() {
        return chain > 0 ? Math.max(0f, chainTimer / GameConfig.CHAIN_WINDOW) : 0f;
    }
}
