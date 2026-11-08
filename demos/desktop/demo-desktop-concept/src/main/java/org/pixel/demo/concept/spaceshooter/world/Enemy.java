package org.pixel.demo.concept.spaceshooter.world;

import org.pixel.demo.concept.spaceshooter.GameConfig;

/**
 * An enemy ship (pooled: {@link #active} says whether this slot is in use).
 */
public final class Enemy {

    public boolean active;
    public EnemyType type;
    public Movement movement;
    public float x, y, vx, vy;
    public float hitPoints, maxHitPoints;
    /** Seconds alive, and in the current {@link #state}. */
    public float age, stateTime;
    /** Behaviour stage (per movement: entering, holding, leaving...). */
    public int state;
    /** Hit flash, 1 right after a hit, fading to 0. */
    public float flash;
    /** Movement parameters set by the spawner (meaning per movement). */
    public float baseY, amplitude, frequency, phase, holdX, speed;
    /** Seconds a holding enemy stays before leaving. */
    public float stay;
    /** Firing timers and pattern state. */
    public float fireTimer, fireTimer2, summonTimer, angle;
    /** Fires one aimed shot (darts in later waves). */
    public boolean armed;
    /** Formation it belongs to (0 = none). */
    public int group;
    /** Breaking apart (bosses): no collisions, blasts until removed. */
    public boolean dying;
    /** Boss attack phase (1 to 3). */
    public int bossPhase;

    void init(EnemyType type, Movement movement, float x, float y, float healthScale) {
        this.active = true;
        this.type = type;
        this.movement = movement;
        this.x = x;
        this.y = y;
        this.vx = this.vy = 0f;
        this.maxHitPoints = Math.max(1f, Math.round(type.hitPoints * healthScale));
        this.hitPoints = maxHitPoints;
        this.age = this.stateTime = 0f;
        this.state = 0;
        this.flash = 0f;
        this.baseY = y;
        this.amplitude = this.frequency = this.phase = this.holdX = this.speed = this.stay = 0f;
        this.fireTimer = this.fireTimer2 = this.summonTimer = this.angle = 0f;
        this.armed = false;
        this.group = 0;
        this.dying = false;
        this.bossPhase = 1;
    }

    /**
     * Whether shots and the player can touch it: on screen (not while arriving from the right edge, where it can't be
     * seen yet), not breaking apart, and not a boss still entering.
     */
    public boolean isSolid() {
        return active && !dying && x < GameConfig.WIDTH - 4f && !(movement == Movement.BOSS && state == 0);
    }

    public float healthFraction() {
        return maxHitPoints > 0 ? Math.max(0f, hitPoints / maxHitPoints) : 0f;
    }

    void setState(int state) {
        this.state = state;
        this.stateTime = 0f;
    }
}
