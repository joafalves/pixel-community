package org.pixel.demo.learning.decs;

import org.pixel.commons.DeltaTime;
import org.pixel.commons.service.ServiceProvider;
import org.pixel.content.ContentManager;
import org.pixel.content.Texture;
import org.pixel.core.Camera2D;
import org.pixel.core.Game;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.decs.component.*;
import org.pixel.demo.learning.decs.system.*;
import org.pixel.ext.decs.GameEntity;
import org.pixel.ext.decs.GameWorld;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.graphics.render.canvas.GLCanvasRenderer;

public class DecsDemo extends Game {

    private ContentManager content;
    private GameWorld world;

    public DecsDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        super.load();
        content = ServiceProvider.get(ContentManager.class);

        world = new GameWorld();
        world.getData().put(this);
        world.getData().put(new GLCanvasRenderer(getViewportWidth(), getViewportHeight()));
        world.getData().put(ServiceProvider.get(SpriteBatch.class));
        world.getData().put(new Camera2D(this)); // Put camera in the world's properties

        world.addSystem(new PauseGameSystem(world)); // Pauses/resumes other systems
        world.addSystem(new PlayerInputGameSystem(world));
        world.addSystem(new MovementGameSystem(world));
        world.addSystem(new CollisionGameSystem(world)); // Update collision boxes
        world.addSystem(new CanvasRenderGameSystem(world)); // Fancy canvas-based rendering
        world.addSystem(new ItemPickupGameSystem(world));
        world.addSystem(new HudRenderGameSystem(world)); // Handles UI notifications
        world.addSystem(new GeneralActionsGameSystem(world)); // Handles general actions

        // Create player
        GameEntity player = world.createEntity();
        world.addComponent(player, new PlayerGameComponent());
        world.addComponent(player, new PositionGameComponent(100, 100));
        world.addComponent(player, new VelocityGameComponent());
        world.addComponent(player, new SpriteGameComponent(content.load("images/red-32x32.png", Texture.class)));
        world.addComponent(player, new CollisionGameComponent(100, 100, 32, 32));
        world.addComponent(player, new InventoryGameComponent());

        // Create item
        GameEntity item = world.createEntity();
        world.addComponent(item, new ItemGameComponent());
        world.addComponent(item, new PositionGameComponent(200, 100));
        world.addComponent(item, new SpriteGameComponent(content.load("images/green-32x32.png", Texture.class)));
        world.addComponent(item, new CollisionGameComponent(200, 100, 32, 32));

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
        settings.setTitle("DECS Demo - Fancy Canvas Rendering - Press [P] to pause!");
        settings.setVsync(true);

        var game = new DecsDemo(settings);
        game.start();
    }
}
