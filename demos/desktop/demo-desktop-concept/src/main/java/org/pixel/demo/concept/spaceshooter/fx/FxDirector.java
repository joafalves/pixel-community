package org.pixel.demo.concept.spaceshooter.fx;

import org.pixel.commons.Color;
import org.pixel.demo.concept.spaceshooter.assets.Assets;
import org.pixel.demo.concept.spaceshooter.render.Palette;
import org.pixel.demo.concept.spaceshooter.world.Enemy;
import org.pixel.demo.concept.spaceshooter.world.EnemyType;
import org.pixel.demo.concept.spaceshooter.world.PickupType;
import org.pixel.demo.concept.spaceshooter.world.Player;
import org.pixel.demo.concept.spaceshooter.world.WorldListener;

import java.util.Random;

/**
 * Turns world events into effects: explosions, sparks, popups, screen shake, shockwaves and screen flashes. Owns the
 * effect systems; the renderer draws them.
 */
public final class FxDirector implements WorldListener {

    private static final float PI = (float) Math.PI;
    /** Scrolling speed of debris (space flows to the left). */
    private static final float DRIFT = -24f;

    public final Particles particles;
    public final Explosions explosions = new Explosions(64);
    public final Popups popups = new Popups(24);
    public final CameraShake shake = new CameraShake();
    public final Shockwave shockwave;

    private final Assets assets;
    private final Random random;
    private float whiteFlash, redFlash;

    public FxDirector(Assets assets, Random random) {
        this.assets = assets;
        this.random = random;
        this.particles = new Particles(1600, random);
        this.shockwave = new Shockwave(assets.shockwaveSource);
    }

    public void update(float dt) {
        particles.update(dt);
        explosions.update(dt);
        popups.update(dt);
        shake.update(dt);
        shockwave.update(dt);
        whiteFlash = Math.max(0f, whiteFlash - dt * 2.5f);
        redFlash = Math.max(0f, redFlash - dt * 2f);
    }

    public void clear() {
        particles.clear();
        explosions.clear();
        popups.clear();
        shake.clear();
        shockwave.clear();
        whiteFlash = redFlash = 0f;
    }

    /** Full-screen white flash, 0 to 1 (the HUD draws it). */
    public float whiteFlash() {
        return whiteFlash;
    }

    /** Full-screen red flash (damage), 0 to 1. */
    public float redFlash() {
        return redFlash;
    }

    // region world events

    @Override
    public void shotFired(float x, float y, boolean byPlayer) {
        if (byPlayer) {
            particles.glow(x, y, 7f, 0.07f, Palette.PLAYER_SHOT);
        } else {
            particles.glow(x, y, 6f, 0.12f, Palette.ENEMY_SHOT);
        }
    }

    @Override
    public void enemyHit(Enemy enemy, float x, float y) {
        particles.burst(x, y, 3, 0f, 2.2f, 50f, 140f, 0.3f, Palette.HOT);
        particles.glow(x, y, 5f, 0.08f, Palette.FIRE);
    }

    @Override
    public void enemyDestroyed(Enemy e, int points, int multiplier) {
        if (e.type == EnemyType.RAIDER) {
            explosions.add(assets.raiderBreakup, e.x, e.y, DRIFT, 26f, 1, true, 0f);
            explosions.add(assets.fireball, e.x - 4f, e.y, DRIFT, 64f, 1, random.nextBoolean(), 0.05f);
            particles.burst(e.x, e.y, 24, 0f, 2f * PI, 40f, 190f, 0.7f, Palette.FIRE);
            particles.ring(e.x, e.y, 34f, 0.45f, Palette.HOT);
            particles.glow(e.x, e.y, 30f, 0.3f, Palette.FIRE);
            shake.add(0.35f);
        } else {
            explosions.add(assets.dartBreakup, e.x, e.y, DRIFT, 30f, 1, true, 0f);
            particles.burst(e.x, e.y, 10, 0f, 2f * PI, 30f, 150f, 0.5f, Palette.FIRE);
            particles.glow(e.x, e.y, 18f, 0.2f, Palette.FIRE);
            shake.add(0.12f);
        }
        StringBuilder text = popups.add(e.x, e.y - 12f, multiplier > 1 ? 7f : 6f,
                multiplier > 1 ? Palette.GOLD : Palette.TEXT);
        text.append(points);
    }

    @Override
    public void bossBlast(Enemy boss, float x, float y) {
        explosions.add(assets.fireball, x, y, DRIFT * 0.3f, 70f, 1, random.nextBoolean(), 0f);
        particles.burst(x, y, 10, 0f, 2f * PI, 40f, 170f, 0.6f, Palette.FIRE);
        particles.glow(x, y, 22f, 0.25f, Palette.HOT);
        shake.add(0.22f);
    }

    @Override
    public void bossDefeated(Enemy boss, int points) {
        explosions.add(assets.fireball, boss.x, boss.y, 0f, 50f, 2, false, 0f);
        explosions.add(assets.raiderBreakup, boss.x, boss.y, DRIFT * 0.5f, 20f, 3, true, 0f);
        for (int i = 0; i < 3; i++) {
            particles.ring(boss.x, boss.y, 70f + i * 45f, 0.6f + i * 0.2f, i == 0 ? Palette.HOT : Palette.FIRE);
        }
        particles.burst(boss.x, boss.y, 90, 0f, 2f * PI, 60f, 320f, 1.2f, Palette.FIRE);
        particles.burst(boss.x, boss.y, 40, 0f, 2f * PI, 30f, 200f, 1.0f, Palette.HOT);
        shockwave.start(boss.x, boss.y, 1f);
        shake.add(1f);
        whiteFlash = 0.8f;
        popups.add(boss.x, boss.y - 30f, 10f, Palette.GOLD).append(points);
    }

    @Override
    public void bossPhase(Enemy boss, int phase) {
        particles.ring(boss.x, boss.y, 80f, 0.5f, Palette.DANGER);
        shake.add(0.45f);
        whiteFlash = Math.max(whiteFlash, 0.25f);
    }

    @Override
    public void formationCleared(float x, float y, int points) {
        popups.add(x, y - 20f, 7f, Palette.GOLD).append("BONUS ").append(points);
        particles.ring(x, y, 30f, 0.4f, Palette.GOLD);
    }

    @Override
    public void playerHit(Player p) {
        particles.burst(p.x, p.y, 16, PI, 2f * PI, 40f, 160f, 0.5f, Palette.SHIELD);
        particles.ring(p.x, p.y, 22f, 0.3f, Palette.SHIELD);
        shake.add(0.5f);
        redFlash = 0.6f;
    }

    @Override
    public void playerDestroyed(Player p) {
        explosions.add(assets.fireball, p.x, p.y, DRIFT, 55f, 1, false, 0f);
        particles.burst(p.x, p.y, 50, 0f, 2f * PI, 40f, 260f, 1.0f, Palette.FIRE);
        particles.burst(p.x, p.y, 20, 0f, 2f * PI, 20f, 140f, 0.8f, Palette.SHIELD);
        particles.ring(p.x, p.y, 60f, 0.6f, Palette.HOT);
        shockwave.start(p.x, p.y, 0.8f);
        shake.add(0.9f);
        redFlash = 0.9f;
    }

    @Override
    public void playerSpawned(Player p) {
        redFlash = 0f;
    }

    @Override
    public void extraLife(Player p) {
        popups.add(p.x, p.y - 20f, 8f, Palette.HEAL).append("1UP");
        particles.ring(p.x, p.y, 40f, 0.5f, Palette.HEAL);
    }

    @Override
    public void bomb(float x, float y) {
        shockwave.start(x, y, 1.2f);
        particles.ring(x, y, 120f, 0.6f, Palette.HOT);
        particles.ring(x, y, 220f, 0.9f, Palette.BOMB);
        particles.glow(x, y, 80f, 0.4f, Palette.HOT);
        shake.add(0.6f);
        whiteFlash = 0.6f;
    }

    @Override
    public void shotCancelled(float x, float y) {
        particles.glow(x, y, 5f, 0.35f, Palette.GOLD);
    }

    @Override
    public void pickupCollected(PickupType type, float x, float y, int points) {
        Color color = Palette.pickup(type);
        particles.ring(x, y, 26f, 0.35f, color);
        particles.burst(x, y, 12, 0f, 2f * PI, 30f, 110f, 0.4f, color);
        StringBuilder text = popups.add(x, y - 14f, 7f, color);
        if (points > 0) {
            text.append(points);
            return;
        }
        switch (type) {
            case POWER -> text.append("POWER UP");
            case BOMB -> text.append("+1 BOMB");
            case REPAIR -> text.append("REPAIRED");
        }
    }

    // endregion
}
