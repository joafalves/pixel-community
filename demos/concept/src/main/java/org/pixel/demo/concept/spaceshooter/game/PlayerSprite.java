/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.demo.concept.spaceshooter.game;

import org.pixel.demo.concept.common.Sprite;
import org.pixel.content.TextureFrame;
import org.pixel.content.TexturePack;
import org.pixel.graphics.render.SpriteBatch;

public class PlayerSprite extends Sprite {

    public static final String IS_MOVING_FORWARD_ATTR = "isMovingForward";

    public PlayerSprite(SpriteBatch spriteBatch, TexturePack texturePack, TextureFrame textureFrame) {
        super(spriteBatch, texturePack, textureFrame);
    }

    @Override
    public void load() {

    }

    @Override
    public void dispose() {

    }
}
