package org.pixel.demo.learning.blueprint;

import org.pixel.blueprint.annotation.Blueprint;
import org.pixel.blueprint.annotation.Component;
import org.pixel.graphics.render.SpriteBatch;

@Blueprint
public class CoreBlueprint {

    @Component
    public SpriteBatch spriteBatch() {
        return SpriteBatch.create(128);
    }

}
