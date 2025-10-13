package org.pixel.demo.learning.decs.events;

import lombok.Getter;
import org.pixel.ext.decs.Entity;

@Getter
public class ItemPickedUpEvent {
    private final Entity itemEntity;

    public ItemPickedUpEvent(Entity itemEntity) {
        this.itemEntity = itemEntity;
    }
}
