package org.pixel.tiled.content.importer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pixel.content.ContentManager;
import org.pixel.content.ImportContext;
import org.pixel.content.Texture;
import org.pixel.content.importer.settings.TextureImporterSettings;
import org.pixel.tiled.content.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static org.lwjgl.BufferUtils.createByteBuffer;

public class TileSetImporterTest {
    @Test
    public void processCase1Isolated() throws IOException {
        TileSetImporter importer = new TileSetImporter();
        String tsxFileName = "Tileset.tsx";
        ImportContext ctx = Mockito.mock(ImportContext.class);
        ContentManager contentManager = Mockito.mock(ContentManager.class);
        Texture texture = Mockito.mock(Texture.class);

        Mockito.when(contentManager.load(Mockito.anyString(), Mockito.eq(Texture.class), Mockito.any())).thenReturn(texture);

        InputStream in = this.getClass().getClassLoader().getResourceAsStream(tsxFileName);

        byte[] bytes = in.readAllBytes();
        ByteBuffer buffer = createByteBuffer(bytes.length);
        buffer.put(bytes).flip();

        Mockito.doReturn(buffer).when(ctx).getBuffer();
        Mockito.when(ctx.getContentManager()).thenReturn(contentManager);

        TileSet tileSet = importer.process(ctx);

        Assertions.assertEquals(16, tileSet.getTileHeight());
        Assertions.assertEquals(16, tileSet.getTileWidth());
        Assertions.assertEquals(4, tileSet.getTileCount());
        Assertions.assertEquals(2, tileSet.getColumns());
        Assertions.assertSame(texture, tileSet.getTexture());
        Assertions.assertTrue(tileSet.getCustomProperties().containsKey("1"));
        Assertions.assertTrue(tileSet.getCustomProperties().containsKey("2"));
        Assertions.assertEquals("true", tileSet.getCustomProperties().get("2"));
        Assertions.assertEquals("hello", tileSet.getCustomProperties().get("1"));
        Assertions.assertEquals("property1", tileSet.getTiles().get(0).getProperties().get("1"));
        Assertions.assertEquals(0, tileSet.getTiles().get(1).getProperties().size());

        Assertions.assertTrue(tileSet.getTiles().get(1).getColliders().get(10) instanceof TiledRectangle);

        TiledRectangle rect12 = (TiledRectangle) tileSet.getTiles().get(1).getColliders().get(10);

        Assertions.assertEquals(2, rect12.getPosition().getX());
        Assertions.assertEquals(0, rect12.getPosition().getY());
        Assertions.assertEquals(15, rect12.getHeight());
        Assertions.assertEquals(13, rect12.getWidth());

        Assertions.assertTrue(tileSet.getTiles().get(1).getColliders().get(6) instanceof TiledEllipse);

        TiledEllipse ellipse6 = (TiledEllipse) tileSet.getTiles().get(1).getColliders().get(6);

        Assertions.assertEquals(2, ellipse6.getPosition().getX());
        Assertions.assertEquals(1.125, ellipse6.getPosition().getY());
        Assertions.assertEquals(4.5625, ellipse6.getHeight());
        Assertions.assertEquals(5.5625, ellipse6.getWidth());

        Assertions.assertTrue(tileSet.getTiles().get(1).getColliders().get(11) instanceof TiledPoint);

        TiledPoint point11 = (TiledPoint) tileSet.getTiles().get(1).getColliders().get(11);

        Assertions.assertEquals(3.66512, point11.getPosition().getX(), 0.01);
        Assertions.assertEquals(8.37742, point11.getPosition().getY(), 0.01);

        Assertions.assertTrue(tileSet.getTiles().get(1).getColliders().get(9) instanceof TiledPolygon);

        TiledPolygon polygon9 = (TiledPolygon) tileSet.getTiles().get(1).getColliders().get(9);

        Assertions.assertEquals(9.92275, polygon9.getPosition().getX(), 0.01);
        Assertions.assertEquals(0.4375, polygon9.getPosition().getY(), 0.01);
        Assertions.assertEquals(5, polygon9.getVertices().size());
        Assertions.assertEquals(0, polygon9.getVertices().get(0).getX(), 0.01);
        Assertions.assertEquals(0, polygon9.getVertices().get(0).getY(), 0.01);
        Assertions.assertEquals(-4.29775, polygon9.getVertices().get(1).getX(), 0.01);
        Assertions.assertEquals(4.03208, polygon9.getVertices().get(1).getY(), 0.01);
        Assertions.assertEquals(-2.38764, polygon9.getVertices().get(2).getX(), 0.01);
        Assertions.assertEquals(11.1007, polygon9.getVertices().get(2).getY(), 0.01);
        Assertions.assertEquals(1.81461, polygon9.getVertices().get(3).getX(), 0.01);
        Assertions.assertEquals(11.25, polygon9.getVertices().get(3).getY(), 0.01);
        Assertions.assertEquals(4.20225, polygon9.getVertices().get(4).getX(), 0.01);
        Assertions.assertEquals(3.78319, polygon9.getVertices().get(4).getY(), 0.01);
    }

    @Test
    public void processCase2Isolated() throws IOException {
        TileSetImporter importer = new TileSetImporter();
        String tsxFileName = "AnimTileset.tsx";
        ImportContext ctx = Mockito.mock(ImportContext.class);
        ContentManager contentManager = Mockito.mock(ContentManager.class);
        Texture texture = Mockito.mock(Texture.class);

        Mockito.when(contentManager.load(Mockito.anyString(), Mockito.eq(Texture.class), Mockito.any())).thenReturn(texture);

        InputStream in = this.getClass().getClassLoader().getResourceAsStream(tsxFileName);

        assert in != null;
        byte[] bytes = in.readAllBytes();
        ByteBuffer buffer = createByteBuffer(bytes.length);
        buffer.put(bytes).flip();

        Mockito.doReturn(buffer).when(ctx).getBuffer();
        Mockito.when(ctx.getContentManager()).thenReturn(contentManager);

        TileSet tileSet = importer.process(ctx);

        Assertions.assertEquals(16, tileSet.getTileHeight());
        Assertions.assertEquals(16, tileSet.getTileWidth());
        Assertions.assertEquals(6, tileSet.getTileCount());
        Assertions.assertEquals(2, tileSet.getColumns());

        Assertions.assertNotNull(tileSet.getTiles().get(4).getAnimation());
        Assertions.assertEquals(500, tileSet.getTiles().get(4).getAnimation().getFrameList().get(0).getDuration());
        Assertions.assertEquals(4, tileSet.getTiles().get(4).getAnimation().getFrameList().get(0).getLocalId());

        Assertions.assertEquals(500, tileSet.getTiles().get(4).getAnimation().getFrameList().get(1).getDuration());
        Assertions.assertEquals(3, tileSet.getTiles().get(4).getAnimation().getFrameList().get(1).getLocalId());

        Assertions.assertEquals(500, tileSet.getTiles().get(4).getAnimation().getFrameList().get(2).getDuration());
        Assertions.assertEquals(5, tileSet.getTiles().get(4).getAnimation().getFrameList().get(2).getLocalId());

        Assertions.assertEquals(500, tileSet.getTiles().get(4).getAnimation().getFrameList().get(3).getDuration());
        Assertions.assertEquals(3, tileSet.getTiles().get(4).getAnimation().getFrameList().get(3).getLocalId());
    }

    @Test
    public void failTexture() throws IOException {
        TileSetImporter importer = new TileSetImporter();
        String tsxFileName = "Tileset.tsx";
        ImportContext ctx = Mockito.mock(ImportContext.class);
        ContentManager contentManager = Mockito.mock(ContentManager.class);

        Mockito.when(contentManager.load(Mockito.anyString(), Mockito.eq(Texture.class))).thenReturn(null);

        InputStream in = this.getClass().getClassLoader().getResourceAsStream(tsxFileName);

        byte[] bytes = in.readAllBytes();
        ByteBuffer buffer = createByteBuffer(bytes.length);
        buffer.put(bytes).flip();

        Mockito.doReturn(buffer).when(ctx).getBuffer();
        Mockito.when(ctx.getContentManager()).thenReturn(contentManager);

        TileSet tileSet = importer.process(ctx);

        Assertions.assertNull(tileSet);
    }

    @Test
    public void importerSettings() throws IOException {
        TileSetImporter importer = new TileSetImporter();
        String tsxFileName = "Tileset.tsx";
        ImportContext ctx = Mockito.mock(ImportContext.class);
        ContentManager contentManager = Mockito.mock(ContentManager.class);
        Texture texture = Mockito.mock(Texture.class);
        TileMapImporterSettings settings = Mockito.mock(TileMapImporterSettings.class);
        TextureImporterSettings textureImporterSettings = Mockito.mock(TextureImporterSettings.class);

        Mockito.when(settings.getTextureImporterSettings()).thenReturn(textureImporterSettings);

        Mockito.when(contentManager.load(Mockito.anyString(), Mockito.eq(Texture.class), Mockito.any())).thenReturn(texture);

        InputStream in = this.getClass().getClassLoader().getResourceAsStream(tsxFileName);

        assert in != null;
        byte[] bytes = in.readAllBytes();
        ByteBuffer buffer = createByteBuffer(bytes.length);
        buffer.put(bytes).flip();

        Mockito.doReturn(buffer).when(ctx).getBuffer();
        Mockito.when(ctx.getContentManager()).thenReturn(contentManager);
        //Mockito.when(ctx.getSettings()).thenReturn(settings);

        TileSet tileSet = importer.process(ctx);

        Mockito.verify(contentManager).load(Mockito.eq("Tileset.png"), Mockito.eq(Texture.class), Mockito.eq(null));

        Mockito.when(ctx.getSettings()).thenReturn(settings);

        tileSet = importer.process(ctx);

        Mockito.verify(contentManager).load(Mockito.eq("Tileset.png"), Mockito.eq(Texture.class), Mockito.same(textureImporterSettings));
    }
}