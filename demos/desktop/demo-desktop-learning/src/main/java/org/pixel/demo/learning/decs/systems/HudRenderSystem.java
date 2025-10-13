package org.pixel.demo.learning.decs.systems;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.event.EventManager;
import org.pixel.commons.service.ServiceProvider;
import org.pixel.content.ContentManager;
import org.pixel.content.Font;
import org.pixel.core.Camera2D;
import org.pixel.demo.learning.decs.events.ItemPickedUpEvent;
import org.pixel.ext.decs.System;
import org.pixel.ext.decs.World;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Vector2;

public class HudRenderSystem extends System {

    private static final float NOTIFICATION_DURATION = 2.0f; // seconds

    private final Camera2D camera;
    private final SpriteBatch spriteBatch;

    private Font font;
    private String notificationMessage;
    private float notificationTimer;

    public HudRenderSystem(World world) {
        super(world);
        this.spriteBatch = world.getProperties().get(SpriteBatch.class);
        this.camera = world.getProperties().get(Camera2D.class);
    }

    @Override
    public boolean init() {
        EventManager.getDefault().subscribe(ItemPickupSystem.ITEM_PICKED_UP_EVENT, ItemPickedUpEvent.class, data -> {
            // onItemPickup
            notificationMessage = "Item Acquired!";
            notificationTimer = NOTIFICATION_DURATION;
        });
        return super.init();
    }

    @Override
    public void load() {
        this.font = ServiceProvider.get(ContentManager.class).load("fonts/gidole-regular.ttf", Font.class);
    }

    @Override
    public void update(DeltaTime delta) {
        if (notificationTimer > 0) {
            notificationTimer -= delta.getElapsed();
        }
    }

    @Override
    public void draw(DeltaTime delta) {
        if (notificationTimer > 0 && notificationMessage != null) {
            spriteBatch.begin(camera.getViewMatrix());
            spriteBatch.drawText(font, notificationMessage, new Vector2(10, 10), Color.BLACK);
            spriteBatch.end();
        }
    }

    @Override
    public void dispose() {
        EventManager.getDefault().unsubscribe(ItemPickupSystem.ITEM_PICKED_UP_EVENT);
        super.dispose();
    }
}
