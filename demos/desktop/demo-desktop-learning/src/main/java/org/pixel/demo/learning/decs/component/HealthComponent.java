package org.pixel.demo.learning.decs.component;

import org.pixel.ext.decs.GameComponent;

public class HealthComponent implements GameComponent {
    private float health;
    private float maxHealth;
    private float damageCooldown;
    private float invincibilityTimer;

    public HealthComponent(float maxHealth) {
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.damageCooldown = 0.5f; // 0.5 seconds of invincibility after hit
        this.invincibilityTimer = 0;
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = Math.max(0, Math.min(health, maxHealth));
    }

    public void damage(float amount) {
        if (invincibilityTimer <= 0) {
            health -= amount;
            invincibilityTimer = damageCooldown;
        }
    }

    public void heal(float amount) {
        health = Math.min(health + amount, maxHealth);
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public boolean isInvincible() {
        return invincibilityTimer > 0;
    }

    public void updateInvincibility(float delta) {
        if (invincibilityTimer > 0) {
            invincibilityTimer -= delta;
        }
    }

    public float getHealthPercentage() {
        return health / maxHealth;
    }
}
