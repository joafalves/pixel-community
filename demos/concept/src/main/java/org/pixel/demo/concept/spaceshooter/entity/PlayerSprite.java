/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.demo.concept.spaceshooter.entity;

import org.pixel.content.TextureFrame;
import org.pixel.demo.concept.spaceshooter.SpaceShooterAttribute;
import org.pixel.demo.concept.spaceshooter.SpaceShooterGame;
import org.pixel.ext.ecs.GameObject;
import org.pixel.ext.ecs.Sprite;
import org.pixel.ext.ecs.component.AutoDisposeComponent;
import org.pixel.ext.ecs.component.ConstantVelocityComponent;
import org.pixel.math.MathHelper;
import org.pixel.math.Vector2;

public class PlayerSprite extends SpaceShipSprite {

    public static final String IS_MOVING_FORWARD_ATTR = "isMovingForward";

    private static final float BULLET_SPEED = 350f;

    public PlayerSprite(TextureFrame textureFrame) {
        super("player", textureFrame, 10);
        subscribeEvents();
    }

    private void shoot() {
        var bulletRight = createBullet(getTransform().getPosition().getX() + 12, getTransform().getPosition().getY());
        var bulletLeft = createBullet(getTransform().getPosition().getX() - 12, getTransform().getPosition().getY());

        var container = (GameObject) getAttributeMap().get(SpaceShooterAttribute.BULLET_CONTAINER);
        container.addChild(bulletRight);
        container.addChild(bulletLeft);
    }

    private Sprite createBullet(float x, float y) {
        var bullet = new Sprite("bullet", (TextureFrame) getAttributeMap().get(SpaceShooterAttribute.BULLET1_FRAME));
        bullet.getTransform().setPosition(x, y);
        bullet.getTransform().setRotation(-MathHelper.PIo2);
        bullet.addComponent(new ConstantVelocityComponent(new Vector2(0, -BULLET_SPEED)));
        bullet.addComponent(new AutoDisposeComponent(2f));

        return bullet;
    }

    private void subscribeEvents() {
        SpaceShooterGame.$.subscribe("player.shoot", $ -> shoot());
    }

}
