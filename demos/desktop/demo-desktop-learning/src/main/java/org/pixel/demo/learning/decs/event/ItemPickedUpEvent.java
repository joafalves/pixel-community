package org.pixel.demo.learning.decs.event;

import org.pixel.ext.decs.GameEntity;

public record ItemPickedUpEvent(GameEntity itemEntity) {
}
