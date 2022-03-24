package org.pixel.ext.tiled.content;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pixel.ext.tiled.view.TiledMapView;
import org.pixel.graphics.render.SpriteBatch;

class TiledObjectGroupTest {
    @Test
    void draw() {
        TiledMap map = Mockito.mock(TiledMap.class);
        TiledObjectGroup tiledObjectGroup = new TiledObjectGroup(map);
        SpriteBatch spriteBatch = Mockito.mock(SpriteBatch.class);
        TiledMapView view = Mockito.mock(TiledMapView.class);

        tiledObjectGroup.draw(view);

        Mockito.verify(view).draw(Mockito.same(tiledObjectGroup));
    }
}