package org.pixel.tiled.content;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pixel.content.Texture;
import org.pixel.math.Rectangle;

public class TileSetTest {

    @Test
    public void disposeTest() {
        Texture texture = Mockito.mock(Texture.class);
        TileSet tileSet = new TileSet(10, 10, 10, 10, texture);

        tileSet.dispose();

        Mockito.verify(texture).dispose();
    }

    @Test
    public void sourceAtFlips() {
        Texture texture = Mockito.mock(Texture.class);
        TileSet tileSet = new TileSet(10, 6, 4, 2, texture);

        Rectangle rectangle = tileSet.sourceAt(0, false, false);

        Assertions.assertEquals(0, rectangle.getX());
        Assertions.assertEquals(0, rectangle.getY());
        Assertions.assertEquals(10, rectangle.getWidth());
        Assertions.assertEquals(6, rectangle.getHeight());

        rectangle = tileSet.sourceAt(1, false, false);

        Assertions.assertEquals(tileSet.getTileWidth(), rectangle.getX());
        Assertions.assertEquals(0, rectangle.getY());
        Assertions.assertEquals(6, rectangle.getHeight());

        rectangle = tileSet.sourceAt(1, false, true);

        Assertions.assertEquals(tileSet.getTileWidth(), rectangle.getX());
        Assertions.assertEquals(6, rectangle.getY());
        Assertions.assertEquals(-6, rectangle.getHeight());

        rectangle = tileSet.sourceAt(2, false, false);

        Assertions.assertEquals(0, rectangle.getX());
        Assertions.assertEquals(10, rectangle.getWidth());
        Assertions.assertEquals(6, rectangle.getHeight());
        Assertions.assertEquals(tileSet.getTileHeight(), rectangle.getY());

        rectangle = tileSet.sourceAt(2, true, false);

        Assertions.assertEquals(10, rectangle.getX());
        Assertions.assertEquals(-10, rectangle.getWidth());

        Assertions.assertEquals(tileSet.getTileHeight(), rectangle.getY());

        rectangle = tileSet.sourceAt(3, false, false);

        Assertions.assertEquals(tileSet.getTileWidth(), rectangle.getX());
        Assertions.assertEquals(tileSet.getTileHeight(), rectangle.getY());
        Assertions.assertEquals(10, rectangle.getWidth());
        Assertions.assertEquals(6, rectangle.getHeight());

        rectangle = tileSet.sourceAt(3, true, true);

        Assertions.assertEquals(2 * tileSet.getTileWidth(), rectangle.getX());
        Assertions.assertEquals(2 * tileSet.getTileHeight(), rectangle.getY());
        Assertions.assertEquals(-10, rectangle.getWidth());
        Assertions.assertEquals(-6, rectangle.getHeight());

        rectangle = tileSet.sourceAt(4, false, false);

        Assertions.assertEquals(0, rectangle.getX());
        Assertions.assertEquals(0, rectangle.getY());
        Assertions.assertEquals(0, rectangle.getWidth());
        Assertions.assertEquals(0, rectangle.getHeight());
    }
}