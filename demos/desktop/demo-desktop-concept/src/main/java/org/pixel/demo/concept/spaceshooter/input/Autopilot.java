package org.pixel.demo.concept.spaceshooter.input;

import org.pixel.demo.concept.spaceshooter.GameConfig;
import org.pixel.demo.concept.spaceshooter.world.Enemy;
import org.pixel.demo.concept.spaceshooter.world.Player;
import org.pixel.demo.concept.spaceshooter.world.Shot;
import org.pixel.demo.concept.spaceshooter.world.World;

/**
 * A simple pilot for the attract mode (and for play-testing): lines up with the nearest enemy, steers away from
 * nearby shots and ships, fires all the time and bombs when cornered on its last hull point. Not a great player, on
 * purpose.
 */
public final class Autopilot {

    private static final float DANGER_RADIUS = 34f;

    public void steer(World world, Controls out) {
        Player p = world.player;
        out.moveX = out.moveY = 0f;
        out.fire = false;
        out.bomb = false;
        if (!p.alive || p.entering) {
            return;
        }
        float targetX = 70f, targetY = (GameConfig.HEIGHT + GameConfig.HUD_HEIGHT) * 0.5f;
        float nearest = Float.MAX_VALUE;
        for (int i = 0; i < World.MAX_ENEMIES; i++) {
            Enemy e = world.enemy(i);
            if (e.isSolid() && e.x > p.x + 20f && e.x - p.x < nearest) {
                nearest = e.x - p.x;
                targetY = e.y;
            }
        }
        for (int i = 0; i < World.MAX_PICKUPS; i++) { // capsules first
            var pickup = world.pickup(i);
            if (pickup.active && pickup.x > p.x - 10f) {
                targetX = Math.max(40f, Math.min(pickup.x - 10f, 140f));
                targetY = pickup.y;
                break;
            }
        }
        float pushX = 0f, pushY = 0f;
        int threats = 0;
        for (int i = 0, n = world.enemyShots.size(); i < n; i++) {
            Shot s = world.enemyShots.get(i);
            if (!s.active) {
                continue;
            }
            // where the shot will be in a moment
            float dx = s.x + s.vx * 0.12f - p.x, dy = s.y + s.vy * 0.12f - p.y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            if (distance < DANGER_RADIUS) {
                float weight = 1f - distance / DANGER_RADIUS;
                pushY -= Math.signum(dy == 0f ? 1f : dy) * weight * 3f;
                pushX -= Math.signum(dx) * weight;
                threats++;
            }
        }
        for (int i = 0; i < World.MAX_ENEMIES; i++) {
            Enemy e = world.enemy(i);
            if (!e.isSolid()) {
                continue;
            }
            float dx = e.x - p.x, dy = e.y - p.y, reach = e.type.radius + 24f;
            if (Math.abs(dx) < reach && Math.abs(dy) < reach) {
                pushY -= Math.signum(dy == 0f ? 1f : dy) * 2f;
                pushX -= Math.signum(dx) * 1.5f;
            }
        }
        out.moveX = clamp((targetX - p.x) / 30f + pushX, -1f, 1f);
        out.moveY = clamp((targetY - p.y) / 18f + pushY, -1f, 1f);
        out.fire = true;
        out.bomb = p.bombs > 0 && p.hull == 1 && threats >= 3 && p.invulnerable <= 0f;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
