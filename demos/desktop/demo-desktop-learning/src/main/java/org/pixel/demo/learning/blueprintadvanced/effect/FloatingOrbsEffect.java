package org.pixel.demo.learning.blueprintadvanced.effect;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.content.Texture;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Vector2;

import java.util.ArrayList;
import java.util.List;

public class FloatingOrbsEffect implements VisualEffect {

    private final List<Orb> orbs = new ArrayList<>();
    private final Texture redTexture;
    private final Texture blueTexture;
    private final float width;
    private final float height;
    private static final int ORB_COUNT = 12;

    public FloatingOrbsEffect(Texture redTexture, Texture blueTexture, float width, float height) {
        this.redTexture = redTexture;
        this.blueTexture = blueTexture;
        this.width = width;
        this.height = height;
        for (int i = 0; i < ORB_COUNT; i++) {
            orbs.add(new Orb());
        }
    }

    @Override
    public void update(DeltaTime delta) {
        for (Orb orb : orbs) {
            orb.x += orb.vx * delta.getElapsed();
            orb.y += orb.vy * delta.getElapsed();

            if (orb.x < 0 || orb.x > width - 32) orb.vx *= -1;
            if (orb.y < 0 || orb.y > height - 32) orb.vy *= -1;
        }
    }

    @Override
    public void draw(SpriteBatch spriteBatch) {
        for (Orb orb : orbs) {
            Texture tex = orb.useRed ? redTexture : blueTexture;
            Color color = new Color(1f, 1f, 1f, 0.7f);
            spriteBatch.draw(tex, new Vector2(orb.x, orb.y), color);
        }
    }

    @Override
    public String getName() {
        return "Floating Orbs";
    }

    private class Orb {
        float x = (float) Math.random() * (width - 32);
        float y = (float) Math.random() * (height - 32);
        float vx = (float) (Math.random() - 0.5) * 120;
        float vy = (float) (Math.random() - 0.5) * 120;
        boolean useRed = Math.random() > 0.5;
    }
}
