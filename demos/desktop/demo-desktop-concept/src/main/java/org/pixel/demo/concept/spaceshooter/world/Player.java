package org.pixel.demo.concept.spaceshooter.world;

import org.pixel.demo.concept.spaceshooter.GameConfig;

/**
 * The player's ship and run state (lives, hull, bombs, weapon power).
 */
public final class Player {

    public float x, y, vx, vy;
    /** On screen and controllable (false while waiting to respawn, or after the last life). */
    public boolean alive;
    /** Flying in from the left after a (re)spawn; no control until in place. */
    public boolean entering;
    public int hull, lives, bombs, power;
    /** Seconds of invulnerability left (after a hit, a respawn or a bomb). */
    public float invulnerable;
    public float respawnTimer, fireCooldown;
    /** Seconds since the last shot (for muzzle flashes). */
    public float sinceShot = 10f;
    /** Seconds since it was last hit (for the hull warning). */
    public float sinceHit = 10f;

    void reset() {
        lives = GameConfig.START_LIVES;
        bombs = GameConfig.START_BOMBS;
        power = 1;
        alive = false;
        respawnTimer = 0f;
        spawn();
    }

    void spawn() {
        alive = true;
        entering = true;
        x = -24f;
        y = (GameConfig.HEIGHT + GameConfig.HUD_HEIGHT) * 0.5f;
        vx = vy = 0f;
        hull = GameConfig.PLAYER_HULL;
        bombs = Math.max(bombs, GameConfig.START_BOMBS);
        invulnerable = GameConfig.SPAWN_INVULNERABILITY;
        fireCooldown = 0.3f;
    }

    /** Whether enemy shots and ships can hurt it now. */
    public boolean isVulnerable() {
        return alive && !entering && invulnerable <= 0f;
    }
}
