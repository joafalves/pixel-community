package org.pixel.demo.learning.decs.component;

import org.pixel.ext.decs.GameComponent;
import org.pixel.ext.decs.GameEntity;

public class ProjectileComponent implements GameComponent {
    private float damage;
    private float lifetime;
    private GameEntity owner; // Who fired this projectile
    private boolean isEnemyProjectile;

    public ProjectileComponent(float damage, float lifetime, GameEntity owner, boolean isEnemyProjectile) {
        this.damage = damage;
        this.lifetime = lifetime;
        this.owner = owner;
        this.isEnemyProjectile = isEnemyProjectile;
    }

    public float getDamage() {
        return damage;
    }

    public float getLifetime() {
        return lifetime;
    }

    public void updateLifetime(float delta) {
        lifetime -= delta;
    }

    public boolean isExpired() {
        return lifetime <= 0;
    }

    public GameEntity getOwner() {
        return owner;
    }

    public boolean isEnemyProjectile() {
        return isEnemyProjectile;
    }
}
