package org.pixel.demo.concept.spaceshooter.fx;

import org.pixel.commons.Color;
import org.pixel.graphics.canvas.Canvas;

import java.util.Random;

/**
 * Light particles (sparks, glows, rings), drawn with canvas shapes on an additive layer. Struct-of-arrays storage;
 * when full, the oldest particles are replaced.
 */
public final class Particles {

    public static final int SPARK = 0, GLOW = 1, RING = 2;

    private final int capacity;
    private final float[] x, y, vx, vy, life, maxLife, size, drag, red, green, blue;
    private final byte[] kind;
    private final boolean[] alive;
    private final Random random;
    private final Color scratch = new Color(), clear = new Color(0f, 0f, 0f, 0f);
    private int cursor, count;

    public Particles(int capacity, Random random) {
        this.capacity = capacity;
        this.random = random;
        x = new float[capacity];
        y = new float[capacity];
        vx = new float[capacity];
        vy = new float[capacity];
        life = new float[capacity];
        maxLife = new float[capacity];
        size = new float[capacity];
        drag = new float[capacity];
        red = new float[capacity];
        green = new float[capacity];
        blue = new float[capacity];
        kind = new byte[capacity];
        alive = new boolean[capacity];
    }

    /**
     * Add one particle.
     *
     * @param drag Fraction of the speed kept per second (1 = none lost).
     */
    public void add(int type, float px, float py, float pvx, float pvy, float seconds, float particleSize, float pdrag,
                    Color color) {
        int i = cursor;
        cursor = (cursor + 1) % capacity;
        if (!alive[i]) {
            count++;
        }
        alive[i] = true;
        kind[i] = (byte) type;
        x[i] = px;
        y[i] = py;
        vx[i] = pvx;
        vy[i] = pvy;
        life[i] = maxLife[i] = seconds;
        size[i] = particleSize;
        drag[i] = pdrag;
        red[i] = color.getRed();
        green[i] = color.getGreen();
        blue[i] = color.getBlue();
    }

    /** Sparks flying out of (px, py) around {@code angle} (radians), {@code cone} wide. */
    public void burst(float px, float py, int n, float angle, float cone, float minSpeed, float maxSpeed,
                      float seconds, Color color) {
        for (int k = 0; k < n; k++) {
            float a = angle + (random.nextFloat() - 0.5f) * cone;
            float s = minSpeed + random.nextFloat() * (maxSpeed - minSpeed);
            add(SPARK, px, py, (float) Math.cos(a) * s, (float) Math.sin(a) * s,
                    seconds * (0.6f + 0.4f * random.nextFloat()), 1f + random.nextFloat(), 0.08f, color);
        }
    }

    public void glow(float px, float py, float radius, float seconds, Color color) {
        add(GLOW, px, py, 0f, 0f, seconds, radius, 1f, color);
    }

    public void ring(float px, float py, float radius, float seconds, Color color) {
        add(RING, px, py, 0f, 0f, seconds, radius, 1f, color);
    }

    public void update(float dt) {
        for (int i = 0; i < capacity; i++) {
            if (!alive[i]) {
                continue;
            }
            life[i] -= dt;
            if (life[i] <= 0f) {
                alive[i] = false;
                count--;
                continue;
            }
            if (drag[i] < 1f) {
                float keep = (float) Math.pow(drag[i], dt);
                vx[i] *= keep;
                vy[i] *= keep;
            }
            x[i] += vx[i] * dt;
            y[i] += vy[i] * dt;
        }
    }

    /** Draw on an additive layer (colors add up: the alpha fades them out). */
    public void draw(Canvas canvas) {
        for (int i = 0; i < capacity; i++) {
            if (!alive[i]) {
                continue;
            }
            float t = life[i] / maxLife[i]; // 1 when born, 0 when gone
            switch (kind[i]) {
                case SPARK -> {
                    scratch.set(red[i], green[i], blue[i], t);
                    float tail = 0.03f + 0.02f * t;
                    canvas.line(x[i], y[i], x[i] - vx[i] * tail, y[i] - vy[i] * tail).width(size[i]).color(scratch);
                }
                case GLOW -> {
                    scratch.set(red[i], green[i], blue[i], t);
                    float r = size[i] * (0.6f + 0.4f * t);
                    canvas.circle(x[i], y[i], r).fillRadial(x[i], y[i], r, scratch, clear);
                }
                default -> {
                    scratch.set(red[i], green[i], blue[i], t * t);
                    float r = size[i] * (1f - t * t * t);
                    canvas.circle(x[i], y[i], Math.max(0.5f, r)).noFill().stroke(1f + 2f * t, scratch);
                }
            }
        }
    }

    public void clear() {
        for (int i = 0; i < capacity; i++) {
            alive[i] = false;
        }
        count = 0;
    }

    public int count() {
        return count;
    }
}
