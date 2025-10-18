package org.pixel.demo.learning.decs.component;

import org.pixel.ext.decs.GameComponent;

public class AnimationComponent implements GameComponent {
    private float rotation;
    private float rotationSpeed;
    private float pulseTime;
    private float pulseSpeed;
    private float scalePulse; // 0-1, affects rendering scale

    public AnimationComponent(float rotationSpeed, float pulseSpeed) {
        this.rotation = 0;
        this.rotationSpeed = rotationSpeed;
        this.pulseTime = 0;
        this.pulseSpeed = pulseSpeed;
        this.scalePulse = 1.0f;
    }

    public void update(float delta) {
        rotation += rotationSpeed * delta;
        pulseTime += pulseSpeed * delta;
        scalePulse = 1.0f + (float) Math.sin(pulseTime) * 0.1f;
    }

    public float getRotation() {
        return rotation;
    }

    public float getScalePulse() {
        return scalePulse;
    }
}
