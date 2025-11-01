package org.pixel.demo.learning.decs.system;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.event.EventBus;
import org.pixel.content.Texture;
import org.pixel.demo.learning.decs.component.*;
import org.pixel.demo.learning.decs.event.WaveCompleteEvent;
import org.pixel.ext.decs.GameEntity;
import org.pixel.ext.decs.GameGroup;
import org.pixel.ext.decs.GameSystem;
import org.pixel.ext.decs.GameWorld;
import org.pixel.math.MathHelper;

public class WaveSystem extends GameSystem {

    private final GameWorld world;
    private final EventBus eventBus;
    private GameGroup enemies;

    private int currentWave;
    private float spawnTimer;
    private int enemiesToSpawn;
    private boolean waveActive;

    private float worldWidth = 800;
    private float worldHeight = 600;

    public WaveSystem(GameWorld world, EventBus eventBus) {
        this.world = world;
        this.eventBus = eventBus;
        this.currentWave = 0;
        this.waveActive = false;
    }

    @Override
    public void load() {
        enemies = world.getGroup(EnemyComponent.class);
        startNextWave();
    }

    @Override
    public void update(DeltaTime delta) {
        // Spawn enemies for current wave
        if (waveActive && enemiesToSpawn > 0) {
            spawnTimer -= delta.getElapsed();
            if (spawnTimer <= 0) {
                spawnEnemy();
                enemiesToSpawn--;
                spawnTimer = 0.5f; // Spawn every 0.5 seconds
            }
        }

        // Check if wave is complete
        if (waveActive && enemiesToSpawn == 0 && enemies.getEntities().isEmpty()) {
            waveActive = false;
            eventBus.publish(new WaveCompleteEvent(currentWave));
            spawnTimer = 3.0f; // 3 second break before next wave
        }

        // Start next wave after break
        if (!waveActive && spawnTimer > 0) {
            spawnTimer -= delta.getElapsed();
            if (spawnTimer <= 0) {
                startNextWave();
            }
        }
    }

    private void startNextWave() {
        currentWave++;
        enemiesToSpawn = 5 + (currentWave * 3); // More enemies each wave
        spawnTimer = 1.0f;
        waveActive = true;
    }

    private void spawnEnemy() {
        // Random spawn position at edge of screen
        float x, y;
        int edge = MathHelper.random(0, 4);
        switch (edge) {
            case 0: // Top
                x = MathHelper.random(0, worldWidth);
                y = -30;
                break;
            case 1: // Bottom
                x = MathHelper.random(0, worldWidth);
                y = worldHeight + 30;
                break;
            case 2: // Left
                x = -30;
                y = MathHelper.random(0, worldHeight);
                break;
            default: // Right
                x = worldWidth + 30;
                y = MathHelper.random(0, worldHeight);
                break;
        }

        // Random enemy type (weighted towards chasers)
        AIComponent.AIType type;
        int roll = MathHelper.random(0, 100);
        if (roll < 50) {
            type = AIComponent.AIType.CHASER;
        } else if (roll < 80) {
            type = AIComponent.AIType.SHOOTER;
        } else {
            type = AIComponent.AIType.DASHER;
        }

        createEnemy(x, y, type);
    }

    private void createEnemy(float x, float y, AIComponent.AIType type) {
        GameEntity enemy = world.createEntity();
        world.addComponent(enemy, new EnemyComponent());
        world.addComponent(enemy, new AIComponent(type));
        world.addComponent(enemy, new PositionGameComponent(x, y));
        world.addComponent(enemy, new VelocityGameComponent());
        world.addComponent(enemy, new CollisionGameComponent(x, y, 24, 24));
        world.addComponent(enemy, new AnimationComponent(2.0f, 3.0f));

        // Different stats based on type
        switch (type) {
            case CHASER:
                world.addComponent(enemy, new HealthComponent(30));
                world.addComponent(enemy, new ScoreValueComponent(10));
                break;
            case SHOOTER:
                world.addComponent(enemy, new HealthComponent(20));
                world.addComponent(enemy, new WeaponComponent(0.5f, 200f, 10f));
                world.addComponent(enemy, new ScoreValueComponent(20));
                break;
            case DASHER:
                world.addComponent(enemy, new HealthComponent(15));
                world.addComponent(enemy, new ScoreValueComponent(15));
                break;
        }
    }

    public int getCurrentWave() {
        return currentWave;
    }

    public int getEnemiesRemaining() {
        return enemies.getEntities().size() + enemiesToSpawn;
    }
}
