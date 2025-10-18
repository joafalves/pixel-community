package org.pixel.demo.learning.decs;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.event.EventBus;
import org.pixel.core.Camera2D;
import org.pixel.core.Game;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.decs.component.*;
import org.pixel.demo.learning.decs.system.*;
import org.pixel.ext.decs.GameEntity;
import org.pixel.ext.decs.GameWorld;
import org.pixel.graphics.render.canvas.GLCanvasRenderer;

/**
 * DECS Demo: "Neon Defenders" - A twin-stick shooter showcasing advanced DECS architecture
 * and fancy Canvas API rendering with neon visual effects.
 *
 * Controls:
 * - WASD: Move player
 * - Mouse Click or Arrow Keys: Shoot
 * - P: Pause/Resume
 *
 * Features:
 * - Multiple enemy types with different AI behaviors
 * - Wave-based enemy spawning with increasing difficulty
 * - Health and damage system with invincibility frames
 * - Projectile weapons with visual trails
 * - Particle explosion effects
 * - Score tracking
 * - Neon-style rendering with gradients and glow effects
 * - Comprehensive HUD with stats
 */
public class DecsDemo extends Game {

    private GameWorld world;
    private EventBus eventBus;

    public DecsDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        super.load();

        // Create event bus for game events
        eventBus = new EventBus();

        // Create world and set up shared data
        world = new GameWorld();
        GLCanvasRenderer canvas = new GLCanvasRenderer(getViewportWidth(), getViewportHeight());
        Camera2D camera = new Camera2D(this);

        world.getData().put(this);
        world.getData().put(canvas);
        world.getData().put(camera);
        world.getData().put(eventBus);

        // Create score and wave systems (needed by HUD)
        ScoreSystem scoreSystem = new ScoreSystem(eventBus);
        WaveSystem waveSystem = new WaveSystem(world, eventBus);

        // Create HUD system (needed by pause system)
        NeonHudSystem hudSystem = new NeonHudSystem(world, canvas, this, scoreSystem, waveSystem);

        // Add all systems in order of execution
        world.addSystem(new PauseGameSystem(world, hudSystem));
        world.addSystem(new PlayerInputGameSystem(world));
        world.addSystem(new WeaponSystem(world));
        world.addSystem(new AISystem(world));
        world.addSystem(new MovementGameSystem(world));
        world.addSystem(new ProjectileSystem(world));
        world.addSystem(new HealthSystem(world, eventBus));
        world.addSystem(new ParticleSystem(world));
        world.addSystem(new CollisionGameSystem(world));
        world.addSystem(waveSystem);
        world.addSystem(scoreSystem);
        world.addSystem(new NeonRenderSystem(world, canvas));
        world.addSystem(hudSystem);

        // Create player entity
        createPlayer(400, 300);

        world.load();
    }

    private void createPlayer(float x, float y) {
        GameEntity player = world.createEntity();
        world.addComponent(player, new PlayerGameComponent());
        world.addComponent(player, new PositionGameComponent(x, y));
        world.addComponent(player, new VelocityGameComponent());
        world.addComponent(player, new CollisionGameComponent(x, y, 32, 32));
        world.addComponent(player, new HealthComponent(100));
        world.addComponent(player, new WeaponComponent(5f, 400f, 15f)); // 5 shots/sec, fast bullets, 15 damage
    }

    @Override
    public void update(DeltaTime delta) {
        super.update(delta);
        world.update(delta);
    }

    @Override
    public void draw(DeltaTime delta) {
        super.draw(delta);
        world.draw(delta);
    }

    @Override
    public void dispose() {
        world.dispose();
        super.dispose();
    }

    public static void main(String[] args) {
        var settings = new WindowSettings(800, 600);
        settings.setTitle("DECS Demo: Neon Defenders - [WASD] Move, [Mouse/Arrows] Shoot, [P] Pause");
        settings.setVsync(true);
        settings.setBackgroundColor(Color.fromHex("#0A0E1A")); // Dark space background

        var game = new DecsDemo(settings);
        game.start();
    }
}

