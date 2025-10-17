package org.pixel.demo.learning.decs.component;

import lombok.Getter;
import org.pixel.ext.decs.GameComponent;
import org.pixel.ext.decs.GameEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * A component that holds the inventory of an entity.
 */
@Getter
public class InventoryGameComponent implements GameComponent {
    private final List<GameEntity> items = new ArrayList<>();

    public void addItem(GameEntity item) {
        items.add(item);
    }

}
