/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.demo.concept.common;

import org.pixel.content.TextureFrame;
import org.pixel.content.TexturePack;
import org.pixel.commons.DeltaTime;
import org.pixel.graphics.Color;
import org.pixel.graphics.render.SpriteBatch;

public abstract class Sprite extends GameObject {

    private final SpriteBatch spriteBatch;
    private final TexturePack texturePack;
    private final TextureFrame textureFrame;

    public Sprite(SpriteBatch spriteBatch, TexturePack texturePack, TextureFrame textureFrame) {
        this.spriteBatch = spriteBatch;
        this.texturePack = texturePack;
        this.textureFrame = textureFrame;
    }

    @Override
    public void draw(DeltaTime delta) {
        spriteBatch.draw(texturePack.getTexture(), getTransform().getPosition(), textureFrame.getSource(),
                Color.WHITE, textureFrame.getPivot(), getTransform().getScale().getX(),
                getTransform().getScale().getY(), getTransform().getRotation());
    }

    public TextureFrame getTextureFrame() {
        return textureFrame;
    }

    public TexturePack getTexturePack() {
        return texturePack;
    }
}
