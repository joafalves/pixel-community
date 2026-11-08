package org.pixel.demo.concept.spaceshooter.fx;

import org.pixel.demo.concept.spaceshooter.assets.Sheet;
import org.pixel.graphics.canvas.Canvas;

/**
 * Sprite-sheet explosions (ships breaking apart, fireballs), drifting with the scroll.
 */
public final class Explosions {

    private final Blast[] blasts;
    private int count;

    public Explosions(int capacity) {
        blasts = new Blast[capacity];
        for (int i = 0; i < capacity; i++) {
            blasts[i] = new Blast();
        }
    }

    /**
     * Play a sheet once at (x, y).
     *
     * @param delay Seconds before it starts (to chain blasts).
     */
    public void add(Sheet sheet, float x, float y, float vx, float fps, int scale, boolean mirror, float delay) {
        Blast slot = null;
        for (Blast b : blasts) {
            if (!b.active) {
                slot = b;
                break;
            }
        }
        if (slot == null) {
            return; // a blast less is not noticed
        }
        slot.active = true;
        slot.sheet = sheet;
        slot.x = x;
        slot.y = y;
        slot.vx = vx;
        slot.fps = fps;
        slot.scale = scale;
        slot.mirror = mirror;
        slot.delay = delay;
        slot.time = 0f;
        count++;
    }

    public void update(float dt) {
        for (Blast b : blasts) {
            if (!b.active) {
                continue;
            }
            if (b.delay > 0f) {
                b.delay -= dt;
                continue;
            }
            b.time += dt;
            b.x += b.vx * dt;
            if (b.time * b.fps >= b.sheet.frameCount()) {
                b.active = false;
                count--;
            }
        }
    }

    /**
     * @param depth Depth in a sorted layer (explosions draw over ships).
     */
    public void draw(Canvas canvas, float depth) {
        for (Blast b : blasts) {
            if (b.active && b.delay <= 0f) {
                b.sheet.draw(canvas, b.sheet.frameAt(b.time, b.fps, false), b.x, b.y, b.scale, b.mirror)
                        .depth(depth);
            }
        }
    }

    public void clear() {
        for (Blast b : blasts) {
            b.active = false;
        }
        count = 0;
    }

    public int count() {
        return count;
    }

    private static final class Blast {
        boolean active, mirror;
        Sheet sheet;
        float x, y, vx, fps, delay, time;
        int scale;
    }
}
