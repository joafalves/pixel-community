package org.pixel.ext.tiled.content;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pixel.ext.tiled.view.TiledObjectGroupView;
import org.pixel.graphics.render.SpriteBatch;

class TiledObjectTest {
    @Test
    void draw() {
        TiledObject tiledObject = Mockito.mock(TiledObject.class);
        TiledObjectGroup group = Mockito.mock(TiledObjectGroup.class);
        SpriteBatch spriteBatch = Mockito.mock(SpriteBatch.class);
        TiledObjectGroupView view = Mockito.mock(TiledObjectGroupView.class);

        tiledObject.draw(group, view);

        Mockito.verifyNoInteractions(view);
    }
}