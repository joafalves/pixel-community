package org.pixel.demo.learning.decs.component;

import lombok.Getter;
import org.pixel.ext.decs.Component;
import org.pixel.ext.decs.Entity;

import java.util.ArrayList;
import java.util.List;

/**
 * A component that holds the inventory of an entity.
 */
@Getter
public class InventoryComponent implements Component {
    private final List<Entity> items = new ArrayList<>();

    public void addItem(Entity item) {
        items.add(item);
    }

}
