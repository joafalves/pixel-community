package org.pixel.demo.learning.decs.component;

import org.pixel.ext.decs.GameComponent;

public class WeaponComponent implements GameComponent {
    private float fireRate; // Shots per second
    private float cooldown;
    private float projectileSpeed;
    private float projectileDamage;
    private float projectileLifetime;

    public WeaponComponent(float fireRate, float projectileSpeed, float projectileDamage) {
        this.fireRate = fireRate;
        this.cooldown = 0;
        this.projectileSpeed = projectileSpeed;
        this.projectileDamage = projectileDamage;
        this.projectileLifetime = 3.0f; // 3 seconds
    }

    public boolean canFire() {
        return cooldown <= 0;
    }

    public void fire() {
        cooldown = 1.0f / fireRate;
    }

    public void updateCooldown(float delta) {
        if (cooldown > 0) {
            cooldown -= delta;
        }
    }

    public float getProjectileSpeed() {
        return projectileSpeed;
    }

    public float getProjectileDamage() {
        return projectileDamage;
    }

    public float getProjectileLifetime() {
        return projectileLifetime;
    }
}
