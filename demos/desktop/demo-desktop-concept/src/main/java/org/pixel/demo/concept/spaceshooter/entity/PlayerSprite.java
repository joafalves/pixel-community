/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.demo.concept.spaceshooter.entity;

import org.pixel.blueprint.annotation.Auto;
import org.pixel.blueprint.annotation.AfterAssembly;
import org.pixel.commons.event.EventManager;
import org.pixel.content.TextureFrame;
import org.pixel.demo.concept.spaceshooter.SpaceShooterAttribute;
import org.pixel.ext.ecs.GameObject;
import org.pixel.ext.ecs.Sprite;
import org.pixel.ext.ecs.component.AutoDisposeComponent;
import org.pixel.ext.ecs.component.ConstantVelocityComponent;
import org.pixel.math.MathHelper;
import org.pixel.math.Vector2;

import static org.pixel.demo.concept.spaceshooter.SpaceShooterEvents.SHOOT;

public class PlayerSprite extends SpaceShipSprite {

    public static final String IS_MOVING_FORWARD_ATTR = "isMovingForward";

    private static final float BULLET_SPEED = 350f;

    @Auto
    private EventManager eventManager;

    public PlayerSprite(TextureFrame textureFrame) {
        super("Player", textureFrame, 10);
    }

    @AfterAssembly
    private void init() {
        subscribeEvents();
    }

    private void shoot() {
        var bulletRight = createBullet(getTransform().getPosition().getX() + 12, getTransform().getPosition().getY());
        var bulletLeft = createBullet(getTransform().getPosition().getX() - 12, getTransform().getPosition().getY());

        var container = (GameObject) getData().get(SpaceShooterAttribute.BULLET_CONTAINER);
        container.addChild(bulletRight);
        container.addChild(bulletLeft);
    }

    private Sprite createBullet(float x, float y) {
        var bullet = new Sprite("bullet", (TextureFrame) getData().get(SpaceShooterAttribute.BULLET1_FRAME));
        bullet.getData().put(SpaceShooterAttribute.BULLET_TYPE, 0); // 0 = player
        bullet.getTransform().setPosition(x, y);
        bullet.getTransform().setRotation(-MathHelper.PIo2);
        bullet.addComponent(new ConstantVelocityComponent(new Vector2(0, -BULLET_SPEED)));
        bullet.addComponent(new AutoDisposeComponent(2f));

        return bullet;
    }

    private void subscribeEvents() {
        eventManager.subscribe(SHOOT, $ -> shoot());
    }

}
