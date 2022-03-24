package org.pixel.ext.tiled.content;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pixel.ext.tiled.view.TileMapView;
import org.pixel.graphics.render.SpriteBatch;

class TileLayerTest {
    @Test
    void draw() {
        TiledMap map = Mockito.mock(TiledMap.class);
        TiledTileLayer tileLayer = new TiledTileLayer(10, 10, map);
        SpriteBatch spriteBatch = Mockito.mock(SpriteBatch.class);
        TileMapView view = Mockito.mock(TileMapView.class);

        tileLayer.draw(view);

        Mockito.verify(view).draw(Mockito.same(tileLayer));
    }
}