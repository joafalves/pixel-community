package org.pixel.ext.tiled.view;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.pixel.content.Texture;
import org.pixel.ext.tiled.content.*;
import org.pixel.graphics.Color;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Boundary;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

class TiledObjectGroupViewTest {
    SpriteBatch spriteBatch;
    Boundary boundary;
    Texture texture;
    TileSet tileSet;
    TileMap tileMap;
    TiledTileObject object1;
    TiledTileObject object2;
    TiledObjectGroup group;
    List<Vector2> positions;

    @BeforeEach
    public void setup() {
        spriteBatch = Mockito.mock(SpriteBatch.class);
        boundary = Mockito.mock(Boundary.class);
        Mockito.when(boundary.overlaps(Mockito.any())).thenReturn(true);
        texture = Mockito.mock(Texture.class);
        tileSet = new TileSet(2, 3, 2, 1, texture);

        group = Mockito.mock(TiledObjectGroup.class);
        object1 = Mockito.mock(TiledTileObject.class);
        object2 = Mockito.mock(TiledTileObject.class);

        LinkedHashMap<Integer, TiledObject> map = new LinkedHashMap<>();
        tileMap = new TileMap();
        List<TileSet> tileSetList = new ArrayList<>();
        tileSetList.add(tileSet);
        tileMap.setTileSets(tileSetList);
        tileSet.setFirstGId(1);

        map.put(1, object1);
        map.put(2, object2);

        Mockito.when(group.getObjects()).thenReturn(map);

        Mockito.when(group.getTileMap()).thenReturn(tileMap);
        Mockito.when(object1.getPosition()).thenReturn(new Vector2(0, 2));
        Mockito.when(object1.getHeight()).thenReturn(3d);
        Mockito.when(object1.getWidth()).thenReturn(1d);
        Mockito.when(object1.getRotation()).thenReturn(5f);
        Mockito.when(object1.getgID()).thenReturn((long) 1 + TiledConstants.VERTICAL_FLIP_FLAG.getBits() + TiledConstants.HORIZONTAL_FLIP_FLAG.getBits());

        Mockito.when(object2.getPosition()).thenReturn(new Vector2(3, 4));
        Mockito.when(object2.getHeight()).thenReturn(3d);
        Mockito.when(object2.getWidth()).thenReturn(2d);
        Mockito.when(object2.getRotation()).thenReturn(0f);
        Mockito.when(object2.getgID()).thenReturn(2L);

        Mockito.when(group.getOffsetX()).thenReturn(0.4);
        Mockito.when(group.getOffsetY()).thenReturn(0.5);

        positions = new ArrayList<>();

        Answer answer = invocation -> {
            TiledObjectGroup group1 = invocation.getArgument(0);
            TiledObjectGroupView groupView1 = invocation.getArgument(1);

            groupView1.draw((TiledTileObject) invocation.getMock(), group1);

            return invocation.getMock();
        };

        Answer getPosition = invocation -> {
            Vector2 position = invocation.getArgument(1);

            positions.add(new Vector2(position.getX(), position.getY()));

            return invocation.getMock();
        };

        Mockito.doAnswer(answer).when(object1).draw(Mockito.any(), Mockito.any());
        Mockito.doAnswer(answer).when(object2).draw(Mockito.any(), Mockito.any());
        Mockito.doAnswer(getPosition).when(spriteBatch).draw(Mockito.any(Texture.class),
                Mockito.any(Vector2.class), Mockito.any(Rectangle.class),
                Mockito.any(Color.class), Mockito.any(Vector2.class),
                Mockito.anyFloat(), Mockito.anyFloat(), Mockito.anyFloat());
    }

    @Test
    public void draw() {
        TiledObjectGroupView groupView = new TiledObjectGroupView(spriteBatch, boundary);

        groupView.draw(group, 0);

        InOrder inOrder = Mockito.inOrder(spriteBatch);

        inOrder.verify(spriteBatch).draw(Mockito.same(texture),
                Mockito.any(), Mockito.eq(new Rectangle(2f, 3f, -2f, -3f)),
                Mockito.same(Color.WHITE), Mockito.same(Vector2.ZERO_ONE),
                Mockito.eq(1f / 2f), Mockito.eq(1f), Mockito.eq(-5f));
        inOrder.verify(spriteBatch).draw(Mockito.same(texture),
                Mockito.any(), Mockito.eq(new Rectangle(0f, 3f, 2f, 3f)),
                Mockito.same(Color.WHITE), Mockito.same(Vector2.ZERO_ONE),
                Mockito.eq(1f), Mockito.eq(1f), Mockito.eq(-0f));

        inOrder.verifyNoMoreInteractions();

        Assertions.assertEquals(new Vector2(0 + 0.4f, 2 + 0.5f), positions.get(0));
        Assertions.assertEquals(new Vector2(3 + 0.4f, 4 + 0.5f), positions.get(1));
    }

    @Test
    void drawOutside() {
        Boundary boundary1 = new Boundary(0 + 0.4f, 2 + 0.5f, 1, 3);
        Mockito.when(boundary.overlaps(Mockito.argThat(argument ->
                argument.getBottomLeft().equals(boundary1.getBottomLeft()) &&
                        argument.getBottomRight().equals(boundary1.getBottomRight()) &&
                        argument.getTopLeft().equals(boundary1.getTopLeft()) &&
                        argument.getTopRight().equals(boundary1.getTopRight())
        ))).thenReturn(false);

        TiledObjectGroupView objectGroupView = new TiledObjectGroupView(spriteBatch, boundary);

        InOrder inOrder = Mockito.inOrder(spriteBatch);

        objectGroupView.draw(group, 0);

        inOrder.verify(spriteBatch, Mockito.times(0)).draw(Mockito.same(texture),
                Mockito.any(), Mockito.eq(new Rectangle(2f, 3f, -2f, -3f)),
                Mockito.same(Color.WHITE), Mockito.same(Vector2.ZERO_ONE),
                Mockito.eq(1f / 2f), Mockito.eq(1f), Mockito.eq(-5f));

        inOrder.verify(spriteBatch).draw(Mockito.same(texture),
                Mockito.any(), Mockito.eq(new Rectangle(0f, 3f, 2f, 3f)),
                Mockito.same(Color.WHITE), Mockito.same(Vector2.ZERO_ONE),
                Mockito.eq(1f), Mockito.eq(1f), Mockito.eq(-0f));

        inOrder.verifyNoMoreInteractions();

        Assertions.assertEquals(new Vector2(3 + 0.4f, 4 + 0.5f), positions.get(0));
    }
}