package org.pixel.ext.tiled.content;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

public class TiledMapTest {
    @Test
    public void disposeTest() {
        TiledMap tileMap = new TiledMap();
        TiledTileSet tileSet1 = Mockito.mock(TiledTileSet.class);
        TiledTileSet tileSet2 = Mockito.mock(TiledTileSet.class);

        tileMap.addTileSet(tileSet1);
        tileMap.addTileSet(tileSet2);

        InOrder inOrder = Mockito.inOrder(tileSet1, tileSet2);

        tileMap.dispose();

        inOrder.verify(tileSet1).dispose();
        inOrder.verify(tileSet2).dispose();
    }
}