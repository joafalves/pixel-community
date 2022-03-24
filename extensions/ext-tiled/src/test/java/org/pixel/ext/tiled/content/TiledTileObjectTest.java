package org.pixel.ext.tiled.content;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pixel.ext.tiled.view.TiledObjectGroupView;
import org.pixel.graphics.render.SpriteBatch;

class TiledTileObjectTest {
    @Test
    void draw() {
        TiledTileObject tiledTileObject = new TiledTileObject();
        SpriteBatch spriteBatch = Mockito.mock(SpriteBatch.class);
        TiledObjectGroupView view = Mockito.mock(TiledObjectGroupView.class);
        TiledObjectGroup group = Mockito.mock(TiledObjectGroup.class);

        tiledTileObject.draw(group, view);

        Mockito.verify(view).draw(Mockito.same(tiledTileObject), Mockito.same(group));
    }
}