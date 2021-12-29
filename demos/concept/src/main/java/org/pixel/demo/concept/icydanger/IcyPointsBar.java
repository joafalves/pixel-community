package org.pixel.demo.concept.icydanger;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.lifecycle.Updatable;
import org.pixel.content.Texture;
import org.pixel.graphics.Color;
import org.pixel.graphics.SpriteDrawable;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.MathHelper;
import org.pixel.math.Rectangle;

@RequiredArgsConstructor
@Setter
public class IcyPointsBar implements Updatable, SpriteDrawable {

    private final static float BAR_HEIGHT = 12f;

    private final Texture texture;
    private final Rectangle playerOneTextureSource;
    private final Rectangle playerTwoTextureSource;
    private final float displayHeight;
    private final float displayWidth;
    private final Rectangle playerOneDisplayArea = new Rectangle();
    private final Rectangle playerTwoDisplayArea = new Rectangle();

    private float value = 0.5f;

    @Override
    public void update(DeltaTime delta) {
        playerOneDisplayArea.set(0, displayHeight - BAR_HEIGHT, value * displayWidth, BAR_HEIGHT);
        playerTwoDisplayArea.set(value * displayWidth, displayHeight - BAR_HEIGHT, displayWidth * (1 - value),
                BAR_HEIGHT);
    }

    @Override
    public void draw(DeltaTime delta, SpriteBatch spriteBatch) {
        spriteBatch.draw(texture, playerOneDisplayArea, playerOneTextureSource, Color.WHITE);
        spriteBatch.draw(texture, playerTwoDisplayArea, playerTwoTextureSource, Color.WHITE);
    }

    public void addValue(float value) {
        this.value = MathHelper.clamp(this.value + value, 0f, 1f);
    }
}
