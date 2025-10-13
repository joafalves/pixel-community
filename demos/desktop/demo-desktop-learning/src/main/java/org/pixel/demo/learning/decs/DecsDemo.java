package org.pixel.demo.learning.decs;

import org.pixel.commons.DeltaTime;
import org.pixel.commons.service.ServiceProvider;
import org.pixel.commons.service.ServiceRegistrar;
import org.pixel.commons.service.ServiceRegistry;
import org.pixel.content.ContentManager;
import org.pixel.content.Texture;
import org.pixel.core.Camera2D;
import org.pixel.core.Game;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.decs.components.*;
import org.pixel.demo.learning.decs.systems.*;
import org.pixel.ext.decs.Entity;
import org.pixel.ext.decs.World;
import org.pixel.graphics.render.SpriteBatch;

import java.util.List;

public class DecsDemo extends Game {

    private ContentManager content;
    private World world;

    public DecsDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        super.load();
        content = ServiceProvider.get(ContentManager.class);

        world = new World();
        world.getProperties().put(ServiceProvider.get(SpriteBatch.class));
        world.getProperties().put(new Camera2D(this)); // Put camera in the world's properties

        world.addSystem(new PauseSystem(world)); // Pauses/resumes other systems
        world.addSystem(new PlayerInputSystem(world));
        world.addSystem(new MovementSystem(world));
        world.addSystem(new CollisionSystem(world)); // Update collision boxes
        world.addSystem(new SpriteRenderSystem(world)); // Gets camera from world properties
        world.addSystem(new ItemPickupSystem(world));
        world.addSystem(new HudRenderSystem(world)); // Handles UI notifications

        // Create player
        Entity player = world.createEntity();
        world.addComponent(player, new PlayerComponent());
        world.addComponent(player, new PositionComponent(100, 100));
        world.addComponent(player, new VelocityComponent());
        world.addComponent(player, new SpriteComponent(content.load("images/red-32x32.png", Texture.class)));
        world.addComponent(player, new CollisionComponent(100, 100, 32, 32));
        world.addComponent(player, new InventoryComponent());

        // Create item
        Entity item = world.createEntity();
        world.addComponent(item, new ItemComponent());
        world.addComponent(item, new PositionComponent(200, 100));
        world.addComponent(item, new SpriteComponent(content.load("images/green-32x32.png", Texture.class)));
        world.addComponent(item, new CollisionComponent(200, 100, 32, 32));

        world.init();
        world.load();
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
        content.dispose();
        super.dispose();
    }

    public static void main(String[] args) {
        var settings = new WindowSettings(800, 600);
        settings.setTitle("DECS Demo");
        settings.setVsync(true);

        var game = new DecsDemo(settings);
        game.start();
    }
}
