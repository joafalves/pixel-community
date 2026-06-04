package org.pixel.demo.learning.blueprintadvanced.effect;

import org.pixel.commons.DeltaTime;
import org.pixel.graphics.render.SpriteBatch;

public interface VisualEffect {
    void update(DeltaTime delta);
    void draw(SpriteBatch spriteBatch);
    String getName();
}
