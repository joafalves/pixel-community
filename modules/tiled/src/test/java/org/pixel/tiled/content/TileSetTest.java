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
    public void sourceAt() {
        Texture texture = Mockito.mock(Texture.class);
        TileSet tileSet = new TileSet(10, 6, 4, 2, texture);

        Rectangle rectangle = tileSet.sourceAt(0);

        Assertions.assertEquals(0, rectangle.getX());
        Assertions.assertEquals(0, rectangle.getY());
        Assertions.assertEquals(10, rectangle.getWidth());
        Assertions.assertEquals(6, rectangle.getHeight());

        rectangle = tileSet.sourceAt(1);

        Assertions.assertEquals(tileSet.getTileWidth(), rectangle.getX());
        Assertions.assertEquals(0, rectangle.getY());

        rectangle = tileSet.sourceAt(2);

        Assertions.assertEquals(0, rectangle.getX());
        Assertions.assertEquals(tileSet.getTileHeight(), rectangle.getY());

        rectangle = tileSet.sourceAt(3);

        Assertions.assertEquals(tileSet.getTileWidth(), rectangle.getX());
        Assertions.assertEquals(tileSet.getTileHeight(), rectangle.getY());

        rectangle = tileSet.sourceAt(4);

        Assertions.assertEquals(0, rectangle.getX());
        Assertions.assertEquals(0, rectangle.getY());
        Assertions.assertEquals(0, rectangle.getWidth());
        Assertions.assertEquals(0, rectangle.getHeight());
    }
}