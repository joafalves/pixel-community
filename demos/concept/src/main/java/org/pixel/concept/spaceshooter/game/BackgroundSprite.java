package org.pixel.concept.spaceshooter.game;

import lombok.RequiredArgsConstructor;
import org.pixel.commons.DeltaTime;
import org.pixel.concept.spaceshooter.content.BackgroundTexture;
import org.pixel.graphics.Color;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;

@RequiredArgsConstructor
public class BackgroundSprite {

    private final BackgroundTexture texture;
    private final Rectangle targetArea;
    private final Rectangle sourceArea = new Rectangle();

    public void update(DeltaTime delta) {
        sourceArea.set(0, sourceArea.getY() - (delta.getElapsed() * 50f), texture.getWidth(), texture.getHeight());
    }

    public void draw(SpriteBatch spriteBatch) {
        spriteBatch.draw(texture, targetArea, sourceArea, Color.WHITE, Vector2.ZERO, 0.f);
    }

}
