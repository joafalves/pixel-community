package org.pixel.tiled.content.importer;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.pixel.content.ContentManager;
import org.pixel.content.ImportContext;
import org.pixel.core.PixelWindow;
import org.pixel.core.WindowSettings;
import org.pixel.tiled.content.TileMap;
import org.pixel.tiled.content.TileSet;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static org.lwjgl.BufferUtils.createByteBuffer;

public class TileMapImporterTest {

    @Test
    public void processCase1Isolated() throws IOException {
        TileMapImporter importer = new TileMapImporter();
        String tmxFileName = "untitled.tmx";
        ImportContext ctx = Mockito.mock(ImportContext.class);
        ContentManager contentManager = Mockito.mock(ContentManager.class);
        TileSet tileSet1 = Mockito.mock(TileSet.class);
        TileSet tileSet2 = Mockito.mock(TileSet.class);

        InputStream in = this.getClass().getClassLoader().getResourceAsStream(tmxFileName);

        byte[] bytes = in.readAllBytes();
        ByteBuffer buffer = createByteBuffer(bytes.length);
        buffer.put(bytes).flip();

        Mockito.doReturn(buffer).when(ctx).getBuffer();
        Mockito.when(contentManager.load(Mockito.matches("Tileset.tsx"), Mockito.eq(TileSet.class))).thenReturn(tileSet1);
        Mockito.when(contentManager.load(Mockito.matches("tes3.tsx"), Mockito.eq(TileSet.class))).thenReturn(tileSet2);
        Mockito.when(ctx.getContentManager()).thenReturn(contentManager);

        TileMap tileMap = importer.process(ctx);

        Assertions.assertEquals(25, tileMap.getHeight());
        Assertions.assertEquals(51, tileMap.getWidth());
        Assertions.assertSame(tileSet1, tileMap.getTileSets().get(0));
        Assertions.assertSame(tileSet2, tileMap.getTileSets().get(1));
        Assertions.assertEquals(25, tileMap.getLayers().get(0).getHeight());
        Assertions.assertEquals(51, tileMap.getLayers().get(0).getWidth());
        Assertions.assertEquals(0, tileMap.getLayers().get(0).getOffsetX());
        Assertions.assertEquals(0, tileMap.getLayers().get(0).getOffsetY());
        Assertions.assertEquals(-1, tileMap.getLayers().get(1).getOffsetX());
        Assertions.assertEquals(7.5, tileMap.getLayers().get(1).getOffsetY());
    }

}