package org.pixel.tiled.content;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.tiled.view.TiledObjectGroupView;

class TiledObjectTest {
    @Test
    void draw() {
        TiledObject tiledObject = new TiledObject();
        TiledObjectGroup group = Mockito.mock(TiledObjectGroup.class);
        SpriteBatch spriteBatch = Mockito.mock(SpriteBatch.class);
        TiledObjectGroupView view = Mockito.mock(TiledObjectGroupView.class);

        tiledObject.draw(spriteBatch, group, view);

        Mockito.verifyNoInteractions(view);
    }
}