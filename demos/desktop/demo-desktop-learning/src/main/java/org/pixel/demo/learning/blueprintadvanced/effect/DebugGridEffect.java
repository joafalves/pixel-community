package org.pixel.demo.learning.blueprintadvanced.effect;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.content.Texture;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Vector2;

public class DebugGridEffect implements VisualEffect {

    private final Texture pixelTexture;
    private final float width;
    private final float height;
    private static final int GRID_SIZE = 50;

    public DebugGridEffect(Texture pixelTexture, float width, float height) {
        this.pixelTexture = pixelTexture;
        this.width = width;
        this.height = height;
    }

    @Override
    public void update(DeltaTime delta) {
    }

    @Override
    public void draw(SpriteBatch spriteBatch) {
        Color color = new Color(0.2f, 0.8f, 0.2f, 0.15f);

        for (int x = 0; x < width; x += GRID_SIZE) {
            for (int y = 0; y < height; y += 2) {
                spriteBatch.draw(pixelTexture, new Vector2(x, y), color);
            }
        }

        for (int y = 0; y < height; y += GRID_SIZE) {
            for (int x = 0; x < width; x += 2) {
                spriteBatch.draw(pixelTexture, new Vector2(x, y), color);
            }
        }
    }

    @Override
    public String getName() {
        return "Debug Grid";
    }
}
