package org.pixel.demo.concept.spaceshooter.entity;

import org.pixel.commons.DeltaTime;
import org.pixel.content.Texture;
import org.pixel.ext.ecs.GameObject;
import org.pixel.graphics.Color;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Rectangle;

public class BackgroundSprite extends GameObject {

    private final Texture texture;
    private final Rectangle textureSource;
    private final Rectangle displayArea;

    public BackgroundSprite(String name, Texture texture, Rectangle displayArea, Rectangle textureSource) {
        super(name);
        this.texture = texture;
        this.displayArea = displayArea;
        this.textureSource = textureSource;
    }

    @Override
    public void update(DeltaTime delta) {
        super.update(delta);
        textureSource.set(0, textureSource.getY() - (delta.getElapsed() * 50f),
                textureSource.getWidth(), textureSource.getHeight());
    }

    @Override
    public void draw(DeltaTime delta, SpriteBatch spriteBatch) {
        spriteBatch.draw(texture, displayArea, textureSource, Color.WHITE);
    }
}
