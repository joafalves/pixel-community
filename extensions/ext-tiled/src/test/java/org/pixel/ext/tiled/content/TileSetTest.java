package org.pixel.ext.tiled.content;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pixel.content.Texture;
import org.pixel.math.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class TileSetTest {

    @Test
    public void disposeTest() {
        Texture texture = Mockito.mock(Texture.class);
        TiledTileSet tileSet = new TiledTileSet(10, 10, 10, 10, texture);

        tileSet.dispose();

        Mockito.verify(texture).dispose();
    }

    @Test
    public void sourceAtFlips() {
        Texture texture = Mockito.mock(Texture.class);
        TiledTileSet tileSet = new TiledTileSet(10, 6, 4, 2, texture);

        Rectangle rectangle = tileSet.sourceAt(0, false, false, 0);

        Assertions.assertEquals(0, rectangle.getX());
        Assertions.assertEquals(0, rectangle.getY());
        Assertions.assertEquals(10, rectangle.getWidth());
        Assertions.assertEquals(6, rectangle.getHeight());

        rectangle = tileSet.sourceAt(1, false, false, 0);

        Assertions.assertEquals(tileSet.getTileWidth(), rectangle.getX());
        Assertions.assertEquals(0, rectangle.getY());
        Assertions.assertEquals(6, rectangle.getHeight());

        rectangle = tileSet.sourceAt(1, false, true, 0);

        Assertions.assertEquals(tileSet.getTileWidth(), rectangle.getX());
        Assertions.assertEquals(6, rectangle.getY());
        Assertions.assertEquals(-6, rectangle.getHeight());

        rectangle = tileSet.sourceAt(2, false, false, 0);

        Assertions.assertEquals(0, rectangle.getX());
        Assertions.assertEquals(10, rectangle.getWidth());
        Assertions.assertEquals(6, rectangle.getHeight());
        Assertions.assertEquals(tileSet.getTileHeight(), rectangle.getY());

        rectangle = tileSet.sourceAt(2, true, false, 0);

        Assertions.assertEquals(10, rectangle.getX());
        Assertions.assertEquals(-10, rectangle.getWidth());

        Assertions.assertEquals(tileSet.getTileHeight(), rectangle.getY());

        rectangle = tileSet.sourceAt(3, false, false, 0);

        Assertions.assertEquals(tileSet.getTileWidth(), rectangle.getX());
        Assertions.assertEquals(tileSet.getTileHeight(), rectangle.getY());
        Assertions.assertEquals(10, rectangle.getWidth());
        Assertions.assertEquals(6, rectangle.getHeight());

        rectangle = tileSet.sourceAt(3, true, true, 0);

        Assertions.assertEquals(2 * tileSet.getTileWidth(), rectangle.getX());
        Assertions.assertEquals(2 * tileSet.getTileHeight(), rectangle.getY());
        Assertions.assertEquals(-10, rectangle.getWidth());
        Assertions.assertEquals(-6, rectangle.getHeight());

        rectangle = tileSet.sourceAt(4, false, false, 0);

        Assertions.assertEquals(0, rectangle.getX());
        Assertions.assertEquals(0, rectangle.getY());
        Assertions.assertEquals(0, rectangle.getWidth());
        Assertions.assertEquals(0, rectangle.getHeight());
    }

    @Test
    void sourceAtFrame() {
        Texture texture = Mockito.mock(Texture.class);
        TiledTileSet tileSet = new TiledTileSet(10, 6, 6, 2, texture);

        TiledTile tile1 = Mockito.mock(TiledTile.class);
        TiledAnimation animation1 = new TiledAnimation();
        TiledFrame frame1 = Mockito.mock(TiledFrame.class);
        TiledFrame frame2 = Mockito.mock(TiledFrame.class);
        TiledFrame frame3 = Mockito.mock(TiledFrame.class);

        Mockito.when(frame1.getDuration()).thenReturn(10L);
        Mockito.when(frame1.getLocalId()).thenReturn(0);

        Mockito.when(frame2.getDuration()).thenReturn(3L);
        Mockito.when(frame2.getLocalId()).thenReturn(1);

        Mockito.when(frame3.getDuration()).thenReturn(5L);
        Mockito.when(frame3.getLocalId()).thenReturn(2);

        List<TiledFrame> frameList = new ArrayList<>();
        frameList.add(frame1);
        frameList.add(frame2);
        frameList.add(frame3);

        animation1.setFrameList(frameList);
        Mockito.when(tile1.getAnimation()).thenReturn(animation1);

        tileSet.setTile(0, tile1);

        Rectangle source1 = tileSet.sourceAt(0, false, false, 0);

        Assertions.assertEquals(0, source1.getX());
        Assertions.assertEquals(0, source1.getY());
        Assertions.assertEquals(6, source1.getHeight());
        Assertions.assertEquals(10, source1.getWidth());

        source1 = tileSet.sourceAt(0, false, false, 5);

        Assertions.assertEquals(0, source1.getX());
        Assertions.assertEquals(0, source1.getY());
        Assertions.assertEquals(6, source1.getHeight());
        Assertions.assertEquals(10, source1.getWidth());

        source1 = tileSet.sourceAt(0, false, false, 10);

        Assertions.assertEquals(10, source1.getX());
        Assertions.assertEquals(0, source1.getY());
        Assertions.assertEquals(6, source1.getHeight());
        Assertions.assertEquals(10, source1.getWidth());

        source1 = tileSet.sourceAt(0, false, false, 13);

        Assertions.assertEquals(0, source1.getX());
        Assertions.assertEquals(6, source1.getY());
        Assertions.assertEquals(6, source1.getHeight());
        Assertions.assertEquals(10, source1.getWidth());

        source1 = tileSet.sourceAt(0, false, false, 18);

        Assertions.assertEquals(0, source1.getX());
        Assertions.assertEquals(0, source1.getY());
        Assertions.assertEquals(6, source1.getHeight());
        Assertions.assertEquals(10, source1.getWidth());

        source1 = tileSet.sourceAt(0, false, false, -10);

        Assertions.assertEquals(10, source1.getX());
        Assertions.assertEquals(0, source1.getY());
        Assertions.assertEquals(6, source1.getHeight());
        Assertions.assertEquals(10, source1.getWidth());
    }
}