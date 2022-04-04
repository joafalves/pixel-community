package org.pixel.ext.tiled.view;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.pixel.content.ContentManager;
import org.pixel.content.ImportContext;
import org.pixel.content.Texture;
import org.pixel.ext.tiled.content.TiledMap;
import org.pixel.ext.tiled.content.TiledTileLayer;
import org.pixel.ext.tiled.content.TiledTileSet;
import org.pixel.ext.tiled.content.importer.TiledMapImporter;
import org.pixel.ext.tiled.content.importer.TiledTileSetImporter;
import org.pixel.graphics.Color;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Boundary;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.BufferUtils.createByteBuffer;

public class TiledLayerViewTest {
    Texture texture1, texture2;
    ImportContext ctx;
    ContentManager contentManager;
    TiledTileSet tileSet1, tileSet2;
    List<Rectangle> displayAreas;
    SpriteBatch spriteBatch;
    Boundary boundary;

    @BeforeEach
    void setup() throws IOException {
        TiledTileSetImporter tileSetImporter = new TiledTileSetImporter();
        texture1 = Mockito.mock(Texture.class);
        texture2 = Mockito.mock(Texture.class);
        displayAreas = new ArrayList<>();
        spriteBatch = Mockito.mock(SpriteBatch.class);
        boundary = Mockito.mock(Boundary.class);

        Mockito.when(boundary.overlaps(Mockito.any(Boundary.class))).thenReturn(true);

        Answer getPosition = invocation -> {
            Rectangle displayArea = invocation.getArgument(1);

            displayAreas.add(new Rectangle(displayArea.getX(), displayArea.getY(), displayArea.getWidth(), displayArea.getHeight()));

            return invocation.getMock();
        };

        ctx = Mockito.mock(ImportContext.class);
        contentManager = Mockito.mock(ContentManager.class);

        Mockito.when(contentManager.load(Mockito.eq("Tileset.png"), Mockito.eq(Texture.class), Mockito.any())).thenReturn(texture1);
        Mockito.when(contentManager.load(Mockito.eq("Tilese2t.png"), Mockito.eq(Texture.class), Mockito.any())).thenReturn(texture2);
        Mockito.when(ctx.getContentManager()).thenReturn(contentManager);
        Mockito.doAnswer(getPosition).when(spriteBatch).draw(Mockito.any(Texture.class),
                Mockito.any(Rectangle.class), Mockito.any(Rectangle.class),
                Mockito.any(Color.class), Mockito.any(Vector2.class), Mockito.anyFloat());

        InputStream in = this.getClass().getClassLoader().getResourceAsStream("Tileset.tsx");

        byte[] bytes = in.readAllBytes();
        ByteBuffer buffer = createByteBuffer(bytes.length);
        buffer.put(bytes).flip();

        Mockito.doReturn(buffer).when(ctx).getBuffer();

        tileSet1 = tileSetImporter.process(ctx);

        in = this.getClass().getClassLoader().getResourceAsStream("tes3.tsx");

        bytes = in.readAllBytes();
        buffer = createByteBuffer(bytes.length);
        buffer.put(bytes).flip();

        Mockito.doReturn(buffer).when(ctx).getBuffer();

        tileSet2 = tileSetImporter.process(ctx);

        Mockito.when(contentManager.load(Mockito.matches("Tileset.tsx"), Mockito.eq(TiledTileSet.class), Mockito.any())).thenReturn(tileSet1);
        Mockito.when(contentManager.load(Mockito.matches("tes3.tsx"), Mockito.eq(TiledTileSet.class), Mockito.any())).thenReturn(tileSet2);
        Mockito.when(ctx.getContentManager()).thenReturn(contentManager);
    }

    @Test
    public void drawCase2() throws IOException {
        TiledMapImporter importer = new TiledMapImporter();

        String tmxFileName = "case2.tmx";

        InputStream in = this.getClass().getClassLoader().getResourceAsStream(tmxFileName);

        byte[] bytes = in.readAllBytes();
        ByteBuffer buffer = createByteBuffer(bytes.length);
        buffer.put(bytes).flip();

        Mockito.doReturn(buffer).when(ctx).getBuffer();

        TiledMap tileMap = importer.process(ctx);

        TiledLayerView layerView = new TiledLayerView(spriteBatch, boundary);

        InOrder inOrder = Mockito.inOrder(spriteBatch);

        layerView.draw((TiledTileLayer) tileMap.getLayers().get(0), 0);

        inOrder.verify(spriteBatch, Mockito.times(2)).draw(Mockito.same(texture2), Mockito.any(),
                Mockito.eq(new Rectangle(0, 0, 16, 16)), Mockito.same(Color.WHITE), Mockito.eq(Vector2.HALF),
                Mockito.eq(0f));

        layerView.draw((TiledTileLayer) tileMap.getLayers().get(1), 0);

        inOrder.verify(spriteBatch).draw(Mockito.same(texture1), Mockito.any(),
                Mockito.eq(new Rectangle(0, 16, 16, 16)), Mockito.same(Color.WHITE), Mockito.eq(Vector2.HALF),
                Mockito.eq(0f));
        inOrder.verify(spriteBatch, Mockito.times(2)).draw(Mockito.same(texture1), Mockito.any(),
                Mockito.eq(new Rectangle(16, 0, 16, 16)), Mockito.same(Color.WHITE), Mockito.eq(Vector2.HALF),
                Mockito.eq(0f));
        inOrder.verify(spriteBatch).draw(Mockito.same(texture1), Mockito.any(),
                Mockito.eq(new Rectangle(0, 16, 16, 16)), Mockito.same(Color.WHITE), Mockito.eq(Vector2.HALF),
                Mockito.eq(0f));

        Assertions.assertEquals(new Rectangle(0 + 8, 8 + 8, 16, 16), displayAreas.get(0));
        Assertions.assertEquals(new Rectangle(16 + 8, 8 + 8, 16, 16), displayAreas.get(1));
        Assertions.assertEquals(new Rectangle(0 + 8, 0 + 8, 16, 16), displayAreas.get(2));
        Assertions.assertEquals(new Rectangle(16 + 8, 8 + 8, 16, 16), displayAreas.get(3));
        Assertions.assertEquals(new Rectangle(0 + 8, 16 + 8, 16, 16), displayAreas.get(4));
        Assertions.assertEquals(new Rectangle(16 + 8, 16 + 8, 16, 16), displayAreas.get(5));
    }

    @Test
    void drawCase4() throws IOException {
        TiledMapImporter importer = new TiledMapImporter();

        String tmxFileName = "case4.tmx";

        InputStream in = this.getClass().getClassLoader().getResourceAsStream(tmxFileName);

        byte[] bytes = in.readAllBytes();
        ByteBuffer buffer = createByteBuffer(bytes.length);
        buffer.put(bytes).flip();

        Mockito.doReturn(buffer).when(ctx).getBuffer();

        TiledMap tileMap = importer.process(ctx);

        TiledLayerView layerView = new TiledLayerView(spriteBatch, boundary);

        InOrder inOrder = Mockito.inOrder(spriteBatch);

        layerView.draw((TiledTileLayer) tileMap.getLayers().get(0), 0);

        inOrder.verify(spriteBatch).draw(Mockito.same(texture1), Mockito.any(),
                Mockito.eq(new Rectangle(0, 0, 16, 16)), Mockito.same(Color.WHITE), Mockito.eq(Vector2.HALF),
                Mockito.eq(0f));

        layerView.draw((TiledTileLayer) tileMap.getLayers().get(1), 0);

        inOrder.verify(spriteBatch).draw(Mockito.same(texture1), Mockito.any(),
                Mockito.eq(new Rectangle(16, 0, 16, 16)), Mockito.same(Color.WHITE), Mockito.eq(Vector2.HALF),
                Mockito.eq(0f));

        Assertions.assertEquals(new Rectangle(0 + 8, 0 + 8, 16, 16), displayAreas.get(0));
        Assertions.assertEquals(new Rectangle((float) Math.floor(0f + 1.5f + 8), 16f - 2f + 8, 16, 16), displayAreas.get(1));
    }

    @Test
    void drawRotations() throws IOException {
        TiledMapImporter importer = new TiledMapImporter();

        String tmxFileName = "rotations.tmx";

        InputStream in = this.getClass().getClassLoader().getResourceAsStream(tmxFileName);

        byte[] bytes = in.readAllBytes();
        ByteBuffer buffer = createByteBuffer(bytes.length);
        buffer.put(bytes).flip();

        Mockito.doReturn(buffer).when(ctx).getBuffer();

        TiledMap tileMap = importer.process(ctx);

        TiledLayerView layerView = new TiledLayerView(spriteBatch, boundary);

        InOrder inOrder = Mockito.inOrder(spriteBatch);

        layerView.draw((TiledTileLayer) tileMap.getLayers().get(0), 0);

        inOrder.verify(spriteBatch).draw(Mockito.same(texture1), Mockito.any(),
                Mockito.eq(new Rectangle(16, 0, 16, 16)), Mockito.same(Color.WHITE), Mockito.eq(Vector2.HALF),
                Mockito.eq(0f));
        inOrder.verify(spriteBatch).draw(Mockito.same(texture1), Mockito.any(),
                Mockito.eq(new Rectangle(16, 16, 16, -16)), Mockito.same(Color.WHITE), Mockito.eq(Vector2.HALF),
                Mockito.eq(0f));
        inOrder.verify(spriteBatch).draw(Mockito.same(texture1), Mockito.any(),
                Mockito.eq(new Rectangle(32, 0, -16, 16)), Mockito.same(Color.WHITE), Mockito.eq(Vector2.HALF),
                Mockito.eq(0f));
        inOrder.verify(spriteBatch).draw(Mockito.same(texture1), Mockito.any(),
                Mockito.eq(new Rectangle(32, 16, -16, -16)), Mockito.same(Color.WHITE), Mockito.eq(Vector2.HALF),
                Mockito.eq(0f));
        inOrder.verify(spriteBatch).draw(Mockito.same(texture1), Mockito.any(),
                Mockito.eq(new Rectangle(32, 16, -16, -16)), Mockito.same(Color.WHITE), Mockito.eq(Vector2.HALF),
                Mockito.eq(-(float) Math.PI / 2));
        inOrder.verify(spriteBatch).draw(Mockito.same(texture2), Mockito.any(),
                Mockito.eq(new Rectangle(16, 0, 16, 16)), Mockito.same(Color.WHITE), Mockito.eq(Vector2.HALF),
                Mockito.eq(-(float) Math.PI / 2));
        inOrder.verify(spriteBatch).draw(Mockito.same(texture1), Mockito.any(),
                Mockito.eq(new Rectangle(32, 0, -16, 16)), Mockito.same(Color.WHITE), Mockito.eq(Vector2.HALF),
                Mockito.eq(-(float) Math.PI / 2));
        inOrder.verify(spriteBatch).draw(Mockito.same(texture2), Mockito.any(),
                Mockito.eq(new Rectangle(16, 16, 16, -16)), Mockito.same(Color.WHITE), Mockito.eq(Vector2.HALF),
                Mockito.eq(-(float) Math.PI / 2));

        Assertions.assertEquals(new Rectangle(0 + 8, 0 + 8, 16, 16), displayAreas.get(0));
        Assertions.assertEquals(new Rectangle(16 + 8, 0 + 8, 16, 16), displayAreas.get(1));
        Assertions.assertEquals(new Rectangle(0 + 8, 16 + 8, 16, 16), displayAreas.get(2));
        Assertions.assertEquals(new Rectangle(16 + 8, 16 + 8, 16, 16), displayAreas.get(3));
        Assertions.assertEquals(new Rectangle(0 + 8, 32 + 8, 16, 16), displayAreas.get(4));
        Assertions.assertEquals(new Rectangle(16 + 8, 32 + 8, 16, 16), displayAreas.get(5));
        Assertions.assertEquals(new Rectangle(0 + 8, 48 + 8, 16, 16), displayAreas.get(6));
        Assertions.assertEquals(new Rectangle(16 + 8, 48 + 8, 16, 16), displayAreas.get(7));
    }

    @Test
    void drawOutside() {
        TiledMap tileMap = new TiledMap();
        TiledTileSet tileSet = new TiledTileSet(16, 16, 4, 2, texture1);
        tileSet.setFirstGId(1);
        tileMap.addTileSet(tileSet);
        tileMap.setTileWidth(16);
        tileMap.setTileHeight(16);
        TiledTileLayer layer = new TiledTileLayer(20, 20, tileMap);
        layer.addTile(0, 0, 1);
        layer.addTile(1, 0, 2);
        tileMap.addLayer(layer);

        Boundary boundary1 = new Boundary(0, 0, 16, 16);

        Mockito.when(boundary.overlaps(Mockito.argThat(argument ->
                argument.getBottomLeft().equals(boundary1.getBottomLeft()) &&
                        argument.getBottomRight().equals(boundary1.getBottomRight()) &&
                        argument.getTopLeft().equals(boundary1.getTopLeft()) &&
                        argument.getTopRight().equals(boundary1.getTopRight())
        ))).thenReturn(false);

        TiledLayerView layerView = new TiledLayerView(spriteBatch, boundary);

        InOrder inOrder = Mockito.inOrder(spriteBatch);

        layerView.draw((TiledTileLayer) tileMap.getLayers().get(0), 0);

        inOrder.verify(spriteBatch, Mockito.times(0)).draw(Mockito.same(texture1), Mockito.any(),
                Mockito.eq(new Rectangle(0, 0, 16, 16)), Mockito.same(Color.WHITE), Mockito.eq(Vector2.HALF),
                Mockito.eq(0f));

        inOrder.verify(spriteBatch).draw(Mockito.same(texture1), Mockito.any(),
                Mockito.eq(new Rectangle(16, 0, 16, 16)), Mockito.same(Color.WHITE), Mockito.eq(Vector2.HALF),
                Mockito.eq(0f));

        Assertions.assertEquals(new Rectangle(16 + 8, 0 + 8, 16, 16), displayAreas.get(0));
    }
}