package org.pixel.demo.concept.spaceshooter.fx;

import org.pixel.demo.concept.spaceshooter.GameConfig;
import org.pixel.graphics.pass.ShaderEffect;

/**
 * A post effect: a ring that bends the scene as it expands (bombs, big explosions). The shader is
 * {@code shaders/shockwave.glsl}; one wave at a time (a new one restarts it).
 */
public final class Shockwave extends ShaderEffect {

    private static final float DURATION = 0.75f, MAX_RADIUS = 0.95f;

    private float centerX, centerY, time = DURATION, strength;

    public Shockwave(String source) {
        super(source);
        set("uThickness", 0.045f);
        set("uWave", 0f, 0f, 0f, 0f);
    }

    /**
     * Start a wave at a point of the virtual screen.
     */
    public void start(float x, float y, float strength) {
        centerX = x / GameConfig.WIDTH;
        centerY = y / GameConfig.HEIGHT;
        this.strength = strength;
        time = 0f;
    }

    public void update(float dt) {
        if (time >= DURATION) {
            set("uWave", 0f, 0f, 0f, 0f);
            return;
        }
        time += dt;
        float t = Math.min(1f, time / DURATION);
        float eased = 1f - (1f - t) * (1f - t); // fast, then slower
        set("uWave", centerX, centerY, eased * MAX_RADIUS, strength * (1f - t));
    }

    public void clear() {
        time = DURATION;
    }
}
