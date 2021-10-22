package org.pixel.tiled.content;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.tiled.view.TileMapView;

class TileLayerTest {
    @Test
    void draw() {
        TileMap map = Mockito.mock(TileMap.class);
        TileLayer tileLayer = new TileLayer(10, 10, map);
        SpriteBatch spriteBatch = Mockito.mock(SpriteBatch.class);
        TileMapView view = Mockito.mock(TileMapView.class);

        tileLayer.draw(spriteBatch, view);

        Mockito.verify(view).draw(Mockito.same(spriteBatch), Mockito.same(tileLayer));
    }
}