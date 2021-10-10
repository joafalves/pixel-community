package org.pixel.tiled.content.importer;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.pixel.content.ContentManager;
import org.pixel.content.ImportContext;
import org.pixel.tiled.content.TileMap;

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

        InputStream in = this.getClass().getClassLoader().getResourceAsStream(tmxFileName);

        byte[] bytes = in.readAllBytes();
        ByteBuffer buffer = createByteBuffer(bytes.length);
        buffer.put(bytes).flip();

        Mockito.doReturn(buffer).when(ctx).getBuffer();

        TileMap tileMap = importer.process(ctx);

        Assertions.assertEquals(tileMap.getHeight(), 25);
        Assertions.assertEquals(tileMap.getWidth(), 51);
    }

    @Test
    public void processCase1Integrated() {
        TileMapImporter importer = new TileMapImporter();
        String tmxFileName = "untitled.tmx";

        ContentManager contentManager = new ContentManager();
        contentManager.addContentImporter(importer);

        TileMap tileMap = contentManager.load(tmxFileName, TileMap.class);

        Assertions.assertEquals(tileMap.getHeight(), 25);
        Assertions.assertEquals(tileMap.getWidth(), 51);
    }
}