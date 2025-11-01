package org.pixel.demo.learning.decs.system;

import org.pixel.commons.DeltaTime;
import org.pixel.commons.event.EventManager;
import org.pixel.demo.learning.decs.component.CollisionGameComponent;
import org.pixel.demo.learning.decs.component.InventoryGameComponent;
import org.pixel.demo.learning.decs.component.ItemGameComponent;
import org.pixel.demo.learning.decs.component.PlayerGameComponent;
import org.pixel.demo.learning.decs.event.ItemPickedUpEvent;
import org.pixel.ext.decs.GameEntity;
import org.pixel.ext.decs.GameGroup;
import org.pixel.ext.decs.GameSystem;
import org.pixel.ext.decs.GameWorld;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.keyboard.KeyboardKey;

import java.util.ArrayList;
import java.util.List;

/**
 * A system that handles item pickup.
 */
public class ItemPickupGameSystem extends GameSystem {

    public static final String ITEM_PICKED_UP_EVENT = "ITEM_PICKED_UP";

    private final GameWorld world;
    private GameGroup players;
    private GameGroup items;
    private EventManager eventManager;

    public ItemPickupGameSystem(GameWorld world) {
        this.world = world;
    }

    @Override
    public void load() {
        this.players = world.getGroup(PlayerGameComponent.class, InventoryGameComponent.class, CollisionGameComponent.class);
        this.items = world.getGroup(ItemGameComponent.class, CollisionGameComponent.class);
        this.eventManager = EventManager.getDefault();
    }

    @Override
    public void update(DeltaTime delta) {
        if (!Keyboard.isKeyPressed(KeyboardKey.E)) {
            return;
        }

        List<GameEntity> itemsToDestroy = new ArrayList<>();

        for (var playerEntity : players) {
            var playerCollision = world.getComponent(playerEntity, CollisionGameComponent.class);
            var playerInventory = world.getComponent(playerEntity, InventoryGameComponent.class);

            for (var itemEntity : items) {
                var itemCollision = world.getComponent(itemEntity, CollisionGameComponent.class);
                if (playerCollision.getBoundingBox().overlaps(itemCollision.getBoundingBox())) {
                    playerInventory.addItem(itemEntity);
                    itemsToDestroy.add(itemEntity);
                    eventManager.publish(ITEM_PICKED_UP_EVENT, new ItemPickedUpEvent(itemEntity));
                }
            }
        }

        for (GameEntity entity : itemsToDestroy) {
            world.destroyEntity(entity);
        }
    }
}
