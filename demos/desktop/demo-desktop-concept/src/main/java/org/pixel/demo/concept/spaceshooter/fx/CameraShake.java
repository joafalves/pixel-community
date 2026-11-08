package org.pixel.demo.concept.spaceshooter.fx;

/**
 * Screen shake from "trauma" (0 to 1): impacts add trauma, which fades; the offset grows with its square, so small
 * hits barely move the view and big ones rattle it.
 */
public final class CameraShake {

    private static final float MAX_OFFSET = 6f, DECAY = 1.5f;

    private float trauma, time, x, y;
    private float intensity = 1f;

    public void add(float amount) {
        trauma = Math.min(1f, trauma + amount * intensity);
    }

    /** A setting: 0 turns shaking off. */
    public void setIntensity(float intensity) {
        this.intensity = Math.max(0f, intensity);
    }

    public void update(float dt) {
        time += dt;
        trauma = Math.max(0f, trauma - DECAY * dt);
        float amount = trauma * trauma * MAX_OFFSET;
        // smooth pseudo-noise: a few incommensurate sines
        x = amount * (float) (Math.sin(time * 61.0) * 0.6 + Math.sin(time * 37.0 + 1.3) * 0.4);
        y = amount * (float) (Math.sin(time * 53.0 + 0.7) * 0.6 + Math.sin(time * 29.0 + 2.1) * 0.4);
    }

    public float offsetX() {
        return x;
    }

    public float offsetY() {
        return y;
    }

    public void clear() {
        trauma = 0f;
        x = y = 0f;
    }
}
