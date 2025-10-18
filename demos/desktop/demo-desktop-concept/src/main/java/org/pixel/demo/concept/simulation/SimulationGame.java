package org.pixel.demo.concept.simulation;

import org.pixel.commons.DeltaTime;
import org.pixel.commons.event.EventBus;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.commons.service.ServiceProvider;
import org.pixel.content.ContentManager;
import org.pixel.core.Game;
import org.pixel.core.WindowSettings;
import org.pixel.demo.concept.simulation.events.WindowSizeChanged;
import org.pixel.demo.concept.simulation.system.DevSystem;
import org.pixel.ext.decs.GameWorld;

public class SimulationGame extends Game {

    private static final Logger log = LoggerFactory.getLogger(SimulationGame.class);

    private GameWorld world;
    private EventBus eventBus;
    private GamePhase activePhase;
    private ContentManager contentManager;

    public SimulationGame(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        // Base load
        world = new GameWorld();
        contentManager = ContentManager.create();
        eventBus = new EventBus();

        // World property definition
        world.getData().put(this);
        world.getData().put(eventBus);
        world.getData().put(contentManager);

        // Register all systems into their scopes
        world.addSystem(new DevSystem(world), GameWorld.DEFAULT_SCOPE);

        // Add other systems for different scopes (even if they don't exist yet)
        // world.addSystem(new NeedsSystem(world), "LIVE");
        // world.addSystem(new BuildModeInputSystem(world), "BUILD");

        // Set initial phase:
        switchPhase(GamePhase.LIVE);

        // Load world (which triggers system loading)
        world.load();
    }

    @Override
    public void update(DeltaTime delta) {
        world.update(delta);
    }

    @Override
    public void draw(DeltaTime delta) {
        world.draw(delta);
    }

    @Override
    public void dispose() {
        world.dispose();
        contentManager.dispose();

        super.dispose();
    }

    @Override
    public void onWindowSizeChange(int width, int height) {
        super.onWindowSizeChange(width, height);

        syncViewportSize();

        if (eventBus != null) {
            eventBus.publish(new WindowSizeChanged(width, height));
        }
    }

    private void switchPhase(GamePhase newPhase) {
        if (activePhase != newPhase && newPhase != null) {
            activePhase = newPhase;
            world.setScope(GameWorld.DEFAULT_SCOPE, newPhase.name());
        }
    }

    public static void main(String[] args) {
        var settings = new WindowSettings(800, 600);
        settings.setTitle("Simulation Concept Demo");
        settings.setVsync(true);
        settings.setWindowResizable(true);

        var game = new SimulationGame(settings);
        game.start();
    }
}
