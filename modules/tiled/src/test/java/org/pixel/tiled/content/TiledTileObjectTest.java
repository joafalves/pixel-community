package org.pixel.tiled.content;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.tiled.view.TiledObjectGroupView;

class TiledTileObjectTest {
    @Test
    void draw() {
        TiledTileObject tiledTileObject = new TiledTileObject();
        SpriteBatch spriteBatch = Mockito.mock(SpriteBatch.class);
        TiledObjectGroupView view = Mockito.mock(TiledObjectGroupView.class);
        TiledObjectGroup group = Mockito.mock(TiledObjectGroup.class);

        tiledTileObject.draw(spriteBatch, group, view);

        Mockito.verify(view).draw(Mockito.same(spriteBatch), Mockito.same(tiledTileObject), Mockito.same(group));
    }
}