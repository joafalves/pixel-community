/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.demo.concept.spaceshooter.entity;

import org.pixel.content.TextureFrame;
import org.pixel.demo.concept.spaceshooter.SpaceShooterGame;
import org.pixel.ext.ecs.Sprite;

public class PlayerSprite extends Sprite {

    public static final String IS_MOVING_FORWARD_ATTR = "isMovingForward";

    public PlayerSprite(TextureFrame textureFrame) {
        super("player", textureFrame);
        subscribeEvents();
    }

    private void shoot() {
        System.out.println("Shoot");
    }

    private void subscribeEvents() {
        SpaceShooterGame.EVENT.subscribe("player.shoot", $ -> shoot());
    }

}
