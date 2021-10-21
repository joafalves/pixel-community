package org.pixel.tiled.view;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.pixel.content.Texture;
import org.pixel.graphics.Color;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;
import org.pixel.tiled.content.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

class TiledObjectGroupViewTest {
    @Test
    public void draw() {
        TiledObjectGroupView groupView = new TiledObjectGroupView();
        SpriteBatch spriteBatch = Mockito.mock(SpriteBatch.class);
        TiledObjectGroup group = Mockito.mock(TiledObjectGroup.class);
        TiledTileObject object1 = Mockito.mock(TiledTileObject.class);
        TiledTileObject object2 = Mockito.mock(TiledTileObject.class);
        LinkedHashMap<Integer, TiledObject> map = new LinkedHashMap<>();
        Texture texture = Mockito.mock(Texture.class);
        TileSet tileSet = new TileSet(2, 3, 2, 1, texture);
        TileMap tileMap = new TileMap();
        List<TileSet> tileSetList = new ArrayList<>();
        tileSetList.add(tileSet);
        tileMap.setTileSets(tileSetList);
        tileSet.setFirstGId(1);

        map.put(1, object1);
        map.put(2, object2);

        Mockito.when(group.getObjects()).thenReturn(map);

        Mockito.when(object1.getTileMap()).thenReturn(tileMap);
        Mockito.when(object1.getPosition()).thenReturn(new Vector2(0, 2));
        Mockito.when(object1.getHeight()).thenReturn(3f);
        Mockito.when(object1.getWidth()).thenReturn(1f);
        Mockito.when(object1.getRotation()).thenReturn(5f);
        Mockito.when(object1.getgID()).thenReturn((long) 1 + TiledConstants.VERTICAL_FLIP_FLAG.getBits() + TiledConstants.HORIZONTAL_FLIP_FLAG.getBits());

        Mockito.when(object2.getTileMap()).thenReturn(tileMap);
        Mockito.when(object2.getPosition()).thenReturn(new Vector2(3, 4));
        Mockito.when(object2.getHeight()).thenReturn(3f);
        Mockito.when(object2.getWidth()).thenReturn(2f);
        Mockito.when(object2.getRotation()).thenReturn(0f);
        Mockito.when(object2.getgID()).thenReturn(2L);

        Mockito.when(group.getOffsetX()).thenReturn(0.4);
        Mockito.when(group.getOffsetY()).thenReturn(0.5);

        List<Vector2> positions = new ArrayList<>();

        Answer answer = invocation -> {
            SpriteBatch spriteBatch1 = invocation.getArgument(0);
            TiledObjectGroup group1 = invocation.getArgument(1);
            TiledObjectGroupView groupView1 = invocation.getArgument(2);

            groupView1.draw(spriteBatch1, (TiledTileObject) invocation.getMock(), group1);

            return invocation.getMock();
        };

        Answer getPosition = invocation -> {
            Vector2 position = invocation.getArgument(1);

            positions.add(new Vector2(position.getX(), position.getY()));

            return invocation.getMock();
        };

        Mockito.doAnswer(answer).when(object1).draw(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.doAnswer(answer).when(object2).draw(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.doAnswer(getPosition).when(spriteBatch).draw(Mockito.any(Texture.class),
                Mockito.any(Vector2.class), Mockito.any(Rectangle.class),
                Mockito.any(Color.class), Mockito.any(Vector2.class),
                Mockito.anyFloat(), Mockito.anyFloat(), Mockito.anyFloat());

        groupView.draw(spriteBatch, group);

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
}