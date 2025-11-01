package org.pixel.demo.learning.decs.event;

import org.pixel.ext.decs.GameEntity;

public class EntityDeathEvent {
    private final GameEntity entity;
    private final float x;
    private final float y;
    private final boolean isEnemy;

    public EntityDeathEvent(GameEntity entity, float x, float y, boolean isEnemy) {
        this.entity = entity;
        this.x = x;
        this.y = y;
        this.isEnemy = isEnemy;
    }

    public GameEntity getEntity() {
        return entity;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public boolean isEnemy() {
        return isEnemy;
    }
}
