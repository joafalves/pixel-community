package org.pixel.demo.learning.decs.system;

import org.pixel.commons.DeltaTime;
import org.pixel.commons.event.EventManager;
import org.pixel.demo.learning.decs.component.CollisionComponent;
import org.pixel.demo.learning.decs.component.InventoryComponent;
import org.pixel.demo.learning.decs.component.ItemComponent;
import org.pixel.demo.learning.decs.component.PlayerComponent;
import org.pixel.demo.learning.decs.event.ItemPickedUpEvent;
import org.pixel.ext.decs.Entity;
import org.pixel.ext.decs.Group;
import org.pixel.ext.decs.System;
import org.pixel.ext.decs.World;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.keyboard.KeyboardKey;

import java.util.ArrayList;
import java.util.List;

/**
 * A system that handles item pickup.
 */
public class ItemPickupSystem extends System {

    public static final String ITEM_PICKED_UP_EVENT = "ITEM_PICKED_UP";

    private Group players;
    private Group items;
    private EventManager eventManager;

    public ItemPickupSystem(World world) {
        super(world);
    }

    @Override
    public void load() {
        this.players = world.getGroup(PlayerComponent.class, InventoryComponent.class, CollisionComponent.class);
        this.items = world.getGroup(ItemComponent.class, CollisionComponent.class);
        this.eventManager = EventManager.getDefault();
    }

    @Override
    public void update(DeltaTime delta) {
        if (!Keyboard.isKeyPressed(KeyboardKey.E)) {
            return;
        }

        List<Entity> itemsToDestroy = new ArrayList<>();

        for (var playerEntity : players) {
            var playerCollision = world.getComponent(playerEntity, CollisionComponent.class);
            var playerInventory = world.getComponent(playerEntity, InventoryComponent.class);

            for (var itemEntity : items) {
                var itemCollision = world.getComponent(itemEntity, CollisionComponent.class);
                if (playerCollision.getBoundingBox().overlaps(itemCollision.getBoundingBox())) {
                    playerInventory.addItem(itemEntity);
                    itemsToDestroy.add(itemEntity);
                    eventManager.publish(ITEM_PICKED_UP_EVENT, new ItemPickedUpEvent(itemEntity));
                }
            }
        }

        for (Entity entity : itemsToDestroy) {
            world.destroyEntity(entity);
        }
    }
}
