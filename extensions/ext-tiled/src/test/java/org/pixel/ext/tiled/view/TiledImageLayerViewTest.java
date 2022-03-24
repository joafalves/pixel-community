package org.pixel.ext.tiled.view;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.pixel.content.Texture;
import org.pixel.ext.tiled.content.TiledMap;
import org.pixel.ext.tiled.content.TiledTileSet;
import org.pixel.ext.tiled.content.TiledImageLayer;
import org.pixel.graphics.Color;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Boundary;
import org.pixel.math.Vector2;

import java.util.ArrayList;
import java.util.List;

class TiledImageLayerViewTest {
    SpriteBatch spriteBatch;
    Boundary boundary;
    TiledTileSet tileSet;
    TiledMap tileMap;
    TiledImageLayer layer;
    Texture texture1;
    Texture texture2;
    List<Vector2> positions;

    @BeforeEach
    public void setup() {
        spriteBatch = Mockito.mock(SpriteBatch.class);
        boundary = Mockito.mock(Boundary.class);
        Mockito.when(boundary.overlaps(Mockito.any())).thenReturn(true);
        texture1 = Mockito.mock(Texture.class);
        texture2 = Mockito.mock(Texture.class);
        tileSet = new TiledTileSet(2, 3, 2, 1, texture1);

        Mockito.when(texture2.getHeight()).thenReturn(10f);
        Mockito.when(texture2.getWidth()).thenReturn(25f);

        tileMap = new TiledMap();
        tileMap.addTileSet(tileSet);
        tileSet.setFirstGId(1);

        layer = new TiledImageLayer(tileMap);
        layer.setOffsetY(1);
        layer.setOffsetX(2);
        layer.setImage(texture2);

        tileMap.addLayer(layer);

        positions = new ArrayList<>();

        Answer getPosition = invocation -> {
            Vector2 position = invocation.getArgument(1);

            positions.add(new Vector2(position.getX(), position.getY()));

            return invocation.getMock();
        };


        Mockito.doAnswer(getPosition).when(spriteBatch).draw(Mockito.any(Texture.class),
                Mockito.any(Vector2.class),
                Mockito.any(Color.class), Mockito.any(Vector2.class));
    }

    @Test
    public void draw() {
        TiledImageLayerView layerView = new TiledImageLayerView(spriteBatch, boundary);

        layerView.draw(layer, 0);

        InOrder inOrder = Mockito.inOrder(spriteBatch);

        inOrder.verify(spriteBatch).draw(Mockito.same(texture2),
                Mockito.any(),
                Mockito.same(Color.WHITE), Mockito.same(Vector2.ZERO));

        inOrder.verifyNoMoreInteractions();

        Assertions.assertEquals(new Vector2(0 + 2, 0 + 1), positions.get(0));
    }

    @Test
    void drawOutside() {
        Boundary boundary1 = new Boundary(0 + 2f, 0 + 1f, texture2.getWidth(), texture2.getHeight());
        Mockito.when(boundary.overlaps(Mockito.argThat(argument ->
                argument.getBottomLeft().equals(boundary1.getBottomLeft()) &&
                        argument.getBottomRight().equals(boundary1.getBottomRight()) &&
                        argument.getTopLeft().equals(boundary1.getTopLeft()) &&
                        argument.getTopRight().equals(boundary1.getTopRight())
        ))).thenReturn(false);

        TiledImageLayerView imageLayerView = new TiledImageLayerView(spriteBatch, boundary);

        InOrder inOrder = Mockito.inOrder(spriteBatch);

        imageLayerView.draw(layer, 0);

        inOrder.verify(spriteBatch, Mockito.times(0)).draw(Mockito.same(texture2),
                Mockito.any(),
                Mockito.same(Color.WHITE), Mockito.same(Vector2.ZERO));

        inOrder.verifyNoMoreInteractions();

        Assertions.assertEquals(0, positions.size());
    }
}