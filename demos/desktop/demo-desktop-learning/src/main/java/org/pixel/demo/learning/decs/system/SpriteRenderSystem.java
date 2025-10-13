package org.pixel.demo.learning.decs.system;

import org.pixel.commons.DeltaTime;
import org.pixel.core.Camera2D;
import org.pixel.demo.learning.decs.component.PositionComponent;
import org.pixel.demo.learning.decs.component.SpriteComponent;
import org.pixel.ext.decs.Group;
import org.pixel.ext.decs.System;
import org.pixel.ext.decs.World;
import org.pixel.graphics.render.SpriteBatch;

/**
 * A system that renders entities with a sprite and position.
 */
public class SpriteRenderSystem extends System {

    private final SpriteBatch spriteBatch;
    private final Camera2D camera;
    private Group renderables;

    public SpriteRenderSystem(World world) {
        super(world);
        this.spriteBatch = world.getProperties().get(SpriteBatch.class);
        this.camera = world.getProperties().get(Camera2D.class);
    }

    @Override
    public void load() {
        this.renderables = world.getGroup(PositionComponent.class, SpriteComponent.class);
    }

    @Override
    public void draw(DeltaTime delta) {
        spriteBatch.begin(camera.getViewMatrix());
        for (var entity : renderables) {
            var position = world.getComponent(entity, PositionComponent.class);
            var sprite = world.getComponent(entity, SpriteComponent.class);
            spriteBatch.draw(sprite.getTexture(), position.getPosition());
        }
        spriteBatch.end();
    }
}
