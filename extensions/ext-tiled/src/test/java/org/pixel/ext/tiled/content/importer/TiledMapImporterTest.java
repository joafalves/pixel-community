package org.pixel.ext.tiled.content.importer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pixel.content.ContentManager;
import org.pixel.content.ImportContext;
import org.pixel.content.importer.settings.TextureImporterSettings;
import org.pixel.ext.tiled.content.TiledTileLayer;
import org.pixel.ext.tiled.content.TiledMap;
import org.pixel.ext.tiled.content.TiledTileSet;
import org.w3c.dom.Document;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.BufferUtils.createByteBuffer;

public class TiledMapImporterTest {

    @Test
    public void processCase1Isolated() throws IOException {
        TileMapImporter importer = new TileMapImporter();
        String tmxFileName = "untitled.tmx";
        ImportContext ctx = Mockito.mock(ImportContext.class);
        ContentManager contentManager = Mockito.mock(ContentManager.class);
        TiledTileSet tileSet1 = Mockito.mock(TiledTileSet.class);
        TiledTileSet tileSet2 = Mockito.mock(TiledTileSet.class);

        InputStream in = this.getClass().getClassLoader().getResourceAsStream(tmxFileName);

        byte[] bytes = in.readAllBytes();
        ByteBuffer buffer = createByteBuffer(bytes.length);
        buffer.put(bytes).flip();

        Mockito.doReturn(buffer).when(ctx).getBuffer();
        Mockito.when(contentManager.load(Mockito.matches("Tileset.tsx"), Mockito.eq(TiledTileSet.class), Mockito.any())).thenReturn(tileSet1);
        Mockito.when(contentManager.load(Mockito.matches("tes3.tsx"), Mockito.eq(TiledTileSet.class), Mockito.any())).thenReturn(tileSet2);
        Mockito.when(ctx.getContentManager()).thenReturn(contentManager);

        TiledMap tileMap = importer.process(ctx);

        Assertions.assertEquals(25, tileMap.getHeight());
        Assertions.assertEquals(51, tileMap.getWidth());
        Assertions.assertEquals(16, tileMap.getTileWidth());
        Assertions.assertEquals(16, tileMap.getTileHeight());
        Assertions.assertSame(tileSet1, tileMap.getTileSets().get(0));
        Mockito.verify(tileSet1).setFirstGId(1);
        Mockito.verify(tileSet2).setFirstGId(5);
        Assertions.assertSame(tileSet2, tileMap.getTileSets().get(1));
        Assertions.assertEquals(0, tileMap.getLayers().get(0).getOffsetX());
        Assertions.assertEquals(0, tileMap.getLayers().get(0).getOffsetY());
        Assertions.assertEquals(7.08, tileMap.getLayers().get(1).getOffsetX(), 0.01);
        Assertions.assertEquals(2.04, tileMap.getLayers().get(1).getOffsetY(), 0.01);
        Assertions.assertEquals("right-down", tileMap.getRenderOrder());
        Assertions.assertTrue(tileMap.getCustomProperties().containsKey("This"));
        Assertions.assertTrue(tileMap.getCustomProperties().containsKey("is property"));
        Assertions.assertEquals("0", tileMap.getCustomProperties().get("This"));
        Assertions.assertEquals("false", tileMap.getCustomProperties().get("is property"));
    }

    @Test
    public void processCase2() throws IOException {
        TileMapImporter importer = new TileMapImporter();
        String tmxFileName = "case2.tmx";
        ImportContext ctx = Mockito.mock(ImportContext.class);
        ContentManager contentManager = Mockito.mock(ContentManager.class);
        TiledTileSet tileSet1 = Mockito.mock(TiledTileSet.class);
        TiledTileSet tileSet2 = Mockito.mock(TiledTileSet.class);

        InputStream in = this.getClass().getClassLoader().getResourceAsStream(tmxFileName);

        byte[] bytes = in.readAllBytes();
        ByteBuffer buffer = createByteBuffer(bytes.length);
        buffer.put(bytes).flip();

        Mockito.doReturn(buffer).when(ctx).getBuffer();
        Mockito.when(contentManager.load(Mockito.matches("Tileset.tsx"), Mockito.eq(TiledTileSet.class))).thenReturn(tileSet1);
        Mockito.when(contentManager.load(Mockito.matches("tes3.tsx"), Mockito.eq(TiledTileSet.class))).thenReturn(tileSet2);
        Mockito.when(ctx.getContentManager()).thenReturn(contentManager);

        TiledMap tileMap = importer.process(ctx);

        TiledTileLayer layer1 = (TiledTileLayer) tileMap.getLayers().get(0);

        Assertions.assertEquals(16, tileMap.getTileWidth());
        Assertions.assertEquals(8, tileMap.getTileHeight());

        Assertions.assertEquals(0, layer1.getTiles()[0][0]);
        Assertions.assertEquals(0, layer1.getTiles()[0][1]);
        Assertions.assertEquals(1, layer1.getTiles()[1][0]);
        Assertions.assertEquals(1, layer1.getTiles()[1][1]);
        Assertions.assertEquals(0, layer1.getTiles()[2][0]);
        Assertions.assertEquals(0, layer1.getTiles()[2][1]);

        TiledTileLayer layer2 = (TiledTileLayer) tileMap.getLayers().get(1);

        Assertions.assertEquals(7, layer2.getTiles()[0][0]);
        Assertions.assertEquals(0, layer2.getTiles()[0][1]);
        Assertions.assertEquals(0, layer2.getTiles()[1][0]);
        Assertions.assertEquals(6, layer2.getTiles()[1][1]);
        Assertions.assertEquals(6, layer2.getTiles()[2][0]);
        Assertions.assertEquals(7, layer2.getTiles()[2][1]);

        Assertions.assertTrue(layer2.getCustomProperties().containsKey("1"));
        Assertions.assertTrue(layer2.getCustomProperties().containsKey("2"));

        Assertions.assertEquals("1.2", layer2.getCustomProperties().get("1"));
        Assertions.assertEquals("property\nlines", layer2.getCustomProperties().get("2"));
    }

    @Test
    public void processCase3() throws IOException {
        TileMapImporter importer = new TileMapImporter();
        String tmxFileName = "case3.tmx";
        ImportContext ctx = Mockito.mock(ImportContext.class);
        ContentManager contentManager = Mockito.mock(ContentManager.class);
        List<TileMapProcessor> processors = new ArrayList<>();
        TileMapProcessor tileMapProcessor = Mockito.mock(TileMapProcessor.class);
        processors.add(tileMapProcessor);
        TextureImporterSettings textureImporterSettings = Mockito.mock(TextureImporterSettings.class);
        TileMapImporterSettings tileMapImporterSettings = Mockito.mock(TileMapImporterSettings.class);
        Mockito.when(tileMapImporterSettings.getTextureImporterSettings()).thenReturn(textureImporterSettings);
        Mockito.when(tileMapImporterSettings.getProcessors()).thenReturn(processors);

        InputStream in = this.getClass().getClassLoader().getResourceAsStream(tmxFileName);

        byte[] bytes = in.readAllBytes();
        ByteBuffer buffer = createByteBuffer(bytes.length);
        buffer.put(bytes).flip();

        Mockito.doReturn(buffer).when(ctx).getBuffer();
        Mockito.when(ctx.getContentManager()).thenReturn(contentManager);
        Mockito.when(ctx.getSettings()).thenReturn(textureImporterSettings);

        TiledMap tileMap = importer.process(ctx);

        Assertions.assertEquals("left-down", tileMap.getRenderOrder());
        Mockito.verify(tileMapProcessor, Mockito.times(0)).process(Mockito.same(tileMap), Mockito.any(Document.class), Mockito.same(ctx));

        Mockito.when(ctx.getSettings()).thenReturn(tileMapImporterSettings);

        tileMap = importer.process(ctx);
        Mockito.verify(tileMapProcessor).process(Mockito.same(tileMap), Mockito.any(Document.class), Mockito.same(ctx));
    }

    @Test
    public void failRead() throws IOException {
        TileMapImporter importer = new TileMapImporter();
        String tsxFileName = "caseFail.tmx";
        ImportContext ctx = Mockito.mock(ImportContext.class);
        ContentManager contentManager = Mockito.mock(ContentManager.class);

        InputStream in = this.getClass().getClassLoader().getResourceAsStream(tsxFileName);

        byte[] bytes = in.readAllBytes();
        ByteBuffer buffer = createByteBuffer(bytes.length);
        buffer.put(bytes).flip();

        Mockito.doReturn(buffer).when(ctx).getBuffer();
        Mockito.when(ctx.getContentManager()).thenReturn(contentManager);

        TiledMap tileSet = importer.process(ctx);

        Assertions.assertNull(tileSet);
    }
}