package org.pixel.demo.learning.blueprintadvanced.effect;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.content.Texture;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Vector2;

import java.util.ArrayList;
import java.util.List;

public class PulseWaveEffect implements VisualEffect {

    private final Texture texture;
    private final float centerX;
    private final float centerY;
    private final List<Wave> waves = new ArrayList<>();
    private float spawnTimer = 0;
    private static final float SPAWN_INTERVAL = 1.5f;

    public PulseWaveEffect(Texture texture, float centerX, float centerY) {
        this.texture = texture;
        this.centerX = centerX;
        this.centerY = centerY;
    }

    @Override
    public void update(DeltaTime delta) {
        spawnTimer += delta.getElapsed();
        if (spawnTimer >= SPAWN_INTERVAL) {
            spawnTimer = 0;
            waves.add(new Wave());
        }

        for (int i = waves.size() - 1; i >= 0; i--) {
            Wave wave = waves.get(i);
            wave.radius += 60 * delta.getElapsed();
            wave.alpha -= 0.5f * delta.getElapsed();
            if (wave.alpha <= 0) {
                waves.remove(i);
            }
        }
    }

    @Override
    public void draw(SpriteBatch spriteBatch) {
        for (Wave wave : waves) {
            Color color = new Color(1f, 0.5f, 0f, wave.alpha);
            float scale = wave.radius / 16f;
            spriteBatch.draw(texture, new Vector2(centerX - wave.radius, centerY - wave.radius), color);
        }
    }

    @Override
    public String getName() {
        return "Pulse Wave";
    }

    private class Wave {
        float radius = 8f;
        float alpha = 0.8f;
    }
}
