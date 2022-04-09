package org.pixel.ext.ldtk;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.pixel.commons.DeltaTime;
import org.pixel.content.Texture;
import org.pixel.graphics.Color;
import org.pixel.graphics.SpriteDrawable;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;

import java.util.List;

@Getter
@Builder(access = AccessLevel.PUBLIC)
public class LdtkGameLevel implements SpriteDrawable {

    private String identifier;
    private Vector2 worldPosition;

    private Color backgroundColor;
    private Texture backgroundTexture;
    private Rectangle backgroundCropArea;
    private Rectangle backgroundDisplayArea;

    private List<LdtkGameLayer> layers;
    private List<LdtkGameEntity> entities;

    @Override
    public void draw(DeltaTime delta, SpriteBatch spriteBatch) {
        drawBackground(spriteBatch);
        drawLayers(delta, spriteBatch);
    }

    private void drawBackground(SpriteBatch spriteBatch) {
        if (backgroundTexture == null) {
            return; // nothing to do...
        }

        spriteBatch.draw(backgroundTexture, backgroundDisplayArea, backgroundCropArea, Color.WHITE, Vector2.ZERO, 0);
    }

    private void drawLayers(DeltaTime delta, SpriteBatch spriteBatch) {
        if (layers == null || layers.isEmpty()) {
            return; // nothing to do...
        }

        for (LdtkGameLayer gameLayer : layers) {
            gameLayer.draw(delta, spriteBatch);
        }
    }
}
