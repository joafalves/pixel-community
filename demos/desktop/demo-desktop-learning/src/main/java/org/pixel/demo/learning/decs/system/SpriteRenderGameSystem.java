package org.pixel.demo.learning.decs.system;

import org.pixel.commons.DeltaTime;
import org.pixel.core.Camera2D;
import org.pixel.demo.learning.decs.component.PositionGameComponent;
import org.pixel.demo.learning.decs.component.SpriteGameComponent;
import org.pixel.ext.decs.GameGroup;
import org.pixel.ext.decs.GameSystem;
import org.pixel.ext.decs.GameWorld;
import org.pixel.graphics.render.SpriteBatch;

/**
 * A system that renders entities with a sprite and position.
 */
public class SpriteRenderGameSystem extends GameSystem {

    private final SpriteBatch spriteBatch;
    private final Camera2D camera;
    private final GameWorld world;
    private GameGroup renderables;

    public SpriteRenderGameSystem(GameWorld world) {
        this.world = world;
        this.spriteBatch = world.getData().get(SpriteBatch.class);
        this.camera = world.getData().get(Camera2D.class);
    }

    @Override
    public void load() {
        this.renderables = world.getGroup(PositionGameComponent.class, SpriteGameComponent.class);
    }

    @Override
    public void draw(DeltaTime delta) {
        spriteBatch.begin(camera.getViewMatrix());
        for (var entity : renderables) {
            var position = world.getComponent(entity, PositionGameComponent.class);
            var sprite = world.getComponent(entity, SpriteGameComponent.class);
            spriteBatch.draw(sprite.getTexture(), position.getPosition());
        }
        spriteBatch.end();
    }
}
