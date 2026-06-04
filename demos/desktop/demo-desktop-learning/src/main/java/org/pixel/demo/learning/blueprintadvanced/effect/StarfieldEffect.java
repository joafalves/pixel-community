package org.pixel.demo.learning.blueprintadvanced.effect;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.content.Texture;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Vector2;

import java.util.ArrayList;
import java.util.List;

public class StarfieldEffect implements VisualEffect {

    private final Texture starTexture;
    private final List<Star> stars = new ArrayList<>();
    private final float width;
    private final float height;
    private static final int STAR_COUNT = 100;

    public StarfieldEffect(Texture starTexture, float width, float height) {
        this.starTexture = starTexture;
        this.width = width;
        this.height = height;
        for (int i = 0; i < STAR_COUNT; i++) {
            stars.add(new Star());
        }
    }

    @Override
    public void update(DeltaTime delta) {
        for (Star star : stars) {
            star.y += star.speed * delta.getElapsed();
            if (star.y > height) {
                star.y = -4;
                star.x = (float) Math.random() * width;
                star.speed = 20f + (float) Math.random() * 100f;
            }
        }
    }

    @Override
    public void draw(SpriteBatch spriteBatch) {
        for (Star star : stars) {
            float alpha = 0.3f + (star.speed / 120f) * 0.7f;
            Color color = new Color(1f, 1f, 1f, alpha);
            Vector2 pos = new Vector2(star.x, star.y);
            spriteBatch.draw(starTexture, pos, color);
        }
    }

    @Override
    public String getName() {
        return "Starfield";
    }

    private class Star {
        float x = (float) Math.random() * width;
        float y = (float) Math.random() * height;
        float speed = 20f + (float) Math.random() * 100f;
    }
}
