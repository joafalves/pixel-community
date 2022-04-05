package org.pixel.ext.tiled.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.pixel.ext.tiled.content.TiledTileLayer;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Boundary;

class DrawStrategyTest {
    TiledTileLayer layer;
    SpriteBatch spriteBatch;
    Boundary boundary;
    long[][] tiles = new long[2][2];

    @BeforeEach
    void setup() {
        layer = Mockito.mock(TiledTileLayer.class);
        spriteBatch = Mockito.mock(SpriteBatch.class);
        boundary = Mockito.mock(Boundary.class);

        Mockito.when(layer.getWidth()).thenReturn(2);
        Mockito.when(layer.getHeight()).thenReturn(2);
        Mockito.when(layer.getTiles()).thenReturn(tiles);
    }

    @Test
    void rightDownStrategy() {
        TiledRightDownStrategy rightDownStrategy = Mockito.spy(new TiledRightDownStrategy());
        Mockito.doNothing().when(rightDownStrategy).drawTile(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyLong());

        InOrder inOrder = Mockito.inOrder(rightDownStrategy);

        rightDownStrategy.draw(spriteBatch, boundary, layer, 0);

        inOrder.verify(rightDownStrategy).drawTile(Mockito.same(spriteBatch), Mockito.same(boundary), Mockito.same(layer), Mockito.eq(0), Mockito.eq(0), Mockito.eq(0L));
        inOrder.verify(rightDownStrategy).drawTile(Mockito.same(spriteBatch), Mockito.same(boundary), Mockito.same(layer), Mockito.eq(1), Mockito.eq(0), Mockito.eq(0L));
        inOrder.verify(rightDownStrategy).drawTile(Mockito.same(spriteBatch), Mockito.same(boundary), Mockito.same(layer), Mockito.eq(0), Mockito.eq(1), Mockito.eq(0L));
        inOrder.verify(rightDownStrategy).drawTile(Mockito.same(spriteBatch), Mockito.same(boundary), Mockito.same(layer), Mockito.eq(1), Mockito.eq(1), Mockito.eq(0L));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void leftDownStrategy() {
        TiledLeftDownStrategy strategy = Mockito.spy(new TiledLeftDownStrategy());
        Mockito.doNothing().when(strategy).drawTile(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyLong());

        InOrder inOrder = Mockito.inOrder(strategy);

        strategy.draw(spriteBatch, boundary, layer, 0);

        inOrder.verify(strategy, Mockito.times(0)).drawTile(Mockito.same(spriteBatch), Mockito.same(boundary), Mockito.same(layer), Mockito.eq(3), Mockito.eq(0), Mockito.eq(0L));
        inOrder.verify(strategy).drawTile(Mockito.same(spriteBatch), Mockito.same(boundary), Mockito.same(layer), Mockito.eq(1), Mockito.eq(0), Mockito.eq(0L));
        inOrder.verify(strategy).drawTile(Mockito.same(spriteBatch), Mockito.same(boundary), Mockito.same(layer), Mockito.eq(0), Mockito.eq(0), Mockito.eq(0L));
        inOrder.verify(strategy).drawTile(Mockito.same(spriteBatch), Mockito.same(boundary), Mockito.same(layer), Mockito.eq(1), Mockito.eq(1), Mockito.eq(0L));
        inOrder.verify(strategy).drawTile(Mockito.same(spriteBatch), Mockito.same(boundary), Mockito.same(layer), Mockito.eq(0), Mockito.eq(1), Mockito.eq(0L));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void leftUpStrategy() {
        TiledLeftUpStrategy strategy = Mockito.spy(new TiledLeftUpStrategy());
        Mockito.doNothing().when(strategy).drawTile(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyLong());

        InOrder inOrder = Mockito.inOrder(strategy);

        strategy.draw(spriteBatch, boundary, layer, 0);

        inOrder.verify(strategy, Mockito.times(0)).drawTile(Mockito.same(spriteBatch), Mockito.same(boundary), Mockito.same(layer), Mockito.eq(3), Mockito.eq(3), Mockito.eq(0L));
        inOrder.verify(strategy, Mockito.times(0)).drawTile(Mockito.same(spriteBatch), Mockito.same(boundary), Mockito.same(layer), Mockito.eq(3), Mockito.eq(0), Mockito.eq(0L));
        inOrder.verify(strategy, Mockito.times(0)).drawTile(Mockito.same(spriteBatch), Mockito.same(boundary), Mockito.same(layer), Mockito.eq(0), Mockito.eq(3), Mockito.eq(0L));
        inOrder.verify(strategy).drawTile(Mockito.same(spriteBatch), Mockito.same(boundary), Mockito.same(layer), Mockito.eq(1), Mockito.eq(1), Mockito.eq(0L));
        inOrder.verify(strategy).drawTile(Mockito.same(spriteBatch), Mockito.same(boundary), Mockito.same(layer), Mockito.eq(0), Mockito.eq(1), Mockito.eq(0L));
        inOrder.verify(strategy).drawTile(Mockito.same(spriteBatch), Mockito.same(boundary), Mockito.same(layer), Mockito.eq(1), Mockito.eq(0), Mockito.eq(0L));
        inOrder.verify(strategy).drawTile(Mockito.same(spriteBatch), Mockito.same(boundary), Mockito.same(layer), Mockito.eq(0), Mockito.eq(0), Mockito.eq(0L));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void rightUpStrategy() {
        TiledRightUpStrategy strategy = Mockito.spy(new TiledRightUpStrategy());
        Mockito.doNothing().when(strategy).drawTile(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyLong());

        InOrder inOrder = Mockito.inOrder(strategy);

        strategy.draw(spriteBatch, boundary, layer, 0);

        inOrder.verify(strategy, Mockito.times(0)).drawTile(Mockito.same(spriteBatch), Mockito.same(boundary), Mockito.same(layer), Mockito.eq(0), Mockito.eq(3), Mockito.eq(0L));
        inOrder.verify(strategy).drawTile(Mockito.same(spriteBatch), Mockito.same(boundary), Mockito.same(layer), Mockito.eq(0), Mockito.eq(1), Mockito.eq(0L));
        inOrder.verify(strategy).drawTile(Mockito.same(spriteBatch), Mockito.same(boundary), Mockito.same(layer), Mockito.eq(1), Mockito.eq(1), Mockito.eq(0L));
        inOrder.verify(strategy).drawTile(Mockito.same(spriteBatch), Mockito.same(boundary), Mockito.same(layer), Mockito.eq(0), Mockito.eq(0), Mockito.eq(0L));
        inOrder.verify(strategy).drawTile(Mockito.same(spriteBatch), Mockito.same(boundary), Mockito.same(layer), Mockito.eq(1), Mockito.eq(0), Mockito.eq(0L));
        inOrder.verifyNoMoreInteractions();
    }
}