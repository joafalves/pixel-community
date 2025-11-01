package org.pixel.demo.learning.decs.component;

import org.pixel.commons.Color;
import org.pixel.ext.decs.GameComponent;

public class ParticleComponent implements GameComponent {
    private float lifetime;
    private float maxLifetime;
    private Color color;
    private float size;
    private float initialSize;
    private boolean shrinks;

    public ParticleComponent(float lifetime, Color color, float size, boolean shrinks) {
        this.lifetime = lifetime;
        this.maxLifetime = lifetime;
        this.color = color;
        this.size = size;
        this.initialSize = size;
        this.shrinks = shrinks;
    }

    public float getLifetime() {
        return lifetime;
    }

    public void updateLifetime(float delta) {
        lifetime -= delta;
        if (shrinks) {
            size = initialSize * (lifetime / maxLifetime);
        }
    }

    public boolean isExpired() {
        return lifetime <= 0;
    }

    public Color getColor() {
        return color;
    }

    public float getAlpha() {
        return lifetime / maxLifetime;
    }

    public float getSize() {
        return size;
    }
}
