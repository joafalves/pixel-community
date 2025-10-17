package org.pixel.demo.learning.decs.system;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.event.EventManager;
import org.pixel.commons.service.ServiceProvider;
import org.pixel.content.ContentManager;
import org.pixel.content.Font;
import org.pixel.core.Camera2D;
import org.pixel.demo.learning.decs.event.ItemPickedUpEvent;
import org.pixel.ext.decs.GameSystem;
import org.pixel.ext.decs.GameWorld;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Vector2;

public class HudRenderGameSystem extends GameSystem {

    private static final float NOTIFICATION_DURATION = 2.0f; // seconds

    private final GameWorld world;
    private final Camera2D camera;
    private final SpriteBatch spriteBatch;

    private Font font;
    private String notificationMessage;
    private float notificationTimer;

    public HudRenderGameSystem(GameWorld world) {
        this.world = world;
        this.spriteBatch = world.getData().get(SpriteBatch.class);
        this.camera = world.getData().get(Camera2D.class);
    }

    @Override
    public void load() {
        EventManager.getDefault().subscribe(ItemPickupGameSystem.ITEM_PICKED_UP_EVENT, ItemPickedUpEvent.class, data -> {
            // onItemPickup
            notificationMessage = "Item Acquired!";
            notificationTimer = NOTIFICATION_DURATION;
        });

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
        EventManager.getDefault().unsubscribe(ItemPickupGameSystem.ITEM_PICKED_UP_EVENT);
        super.dispose();
    }
}
