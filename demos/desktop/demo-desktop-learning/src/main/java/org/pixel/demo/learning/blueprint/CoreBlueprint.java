package org.pixel.demo.learning.blueprint;

import org.pixel.blueprint.annotation.Blueprint;
import org.pixel.blueprint.annotation.Component;
import org.pixel.commons.service.ServiceProvider;
import org.pixel.graphics.render.SpriteBatch;

@Blueprint
public class CoreBlueprint {

    @Component
    public SpriteBatch spriteBatch() {
        var spriteBatch = ServiceProvider.get(SpriteBatch.class);
        spriteBatch.resizeBuffer(128);

        return spriteBatch;
    }

}
