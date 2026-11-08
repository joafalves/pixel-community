package org.pixel.demo.concept.spaceshooter.world;

import org.pixel.demo.concept.spaceshooter.GameConfig;

/**
 * A fixed set of shots, reused: nothing is allocated while playing. A full pool drops new shots (it never happens
 * with the sizes used, and a missing bullet is better than a hitch).
 */
public final class ShotPool {

    private static final float MARGIN = 24f; // shots this far off screen are gone

    private final Shot[] shots;
    private int count; // active shots (for statistics)

    public ShotPool(int capacity) {
        shots = new Shot[capacity];
        for (int i = 0; i < capacity; i++) {
            shots[i] = new Shot();
        }
    }

    public Shot spawn(float x, float y, float vx, float vy, float radius, float damage) {
        for (Shot shot : shots) {
            if (!shot.active) {
                shot.active = true;
                shot.x = x;
                shot.y = y;
                shot.vx = vx;
                shot.vy = vy;
                shot.radius = radius;
                shot.damage = damage;
                shot.age = 0f;
                count++;
                return shot;
            }
        }
        return null;
    }

    void update(float dt) {
        for (Shot shot : shots) {
            if (shot.active) {
                shot.x += shot.vx * dt;
                shot.y += shot.vy * dt;
                shot.age += dt;
                if (shot.x < -MARGIN || shot.x > GameConfig.WIDTH + MARGIN || shot.y < -MARGIN
                        || shot.y > GameConfig.HEIGHT + MARGIN) {
                    remove(shot);
                }
            }
        }
    }

    void remove(Shot shot) {
        if (shot.active) {
            shot.active = false;
            count--;
        }
    }

    void clear() {
        for (Shot shot : shots) {
            shot.active = false;
        }
        count = 0;
    }

    public int size() {
        return shots.length;
    }

    public Shot get(int index) {
        return shots[index];
    }

    public int activeCount() {
        return count;
    }
}
