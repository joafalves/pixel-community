package org.pixel.tiled.content.importer;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.pixel.content.ContentManager;
import org.pixel.content.ImportContext;
import org.pixel.tiled.content.TileMap;
import org.pixel.tiled.content.TileSet;

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

        InputStream in = this.getClass().getClassLoader().getResourceAsStream(tsxFileName);

        byte[] bytes = in.readAllBytes();
        ByteBuffer buffer = createByteBuffer(bytes.length);
        buffer.put(bytes).flip();

        Mockito.doReturn(buffer).when(ctx).getBuffer();

        TileSet tileSet = importer.process(ctx);

        Assertions.assertEquals(tileSet.getTileHeight(), 16);
        Assertions.assertEquals(tileSet.getTileWidth(), 16);
        Assertions.assertEquals(tileSet.getTileCount(), 4);
        Assertions.assertEquals(tileSet.getColumns(), 2);
    }

    @Test
    public void processCase1Integrated() {
        TileSetImporter importer = new TileSetImporter();
        String tsxFileName = "Tileset.tsx";

        ContentManager contentManager = new ContentManager();
        contentManager.addContentImporter(importer);

        TileSet tileSet = contentManager.load(tsxFileName, TileSet.class);

        Assertions.assertEquals(tileSet.getTileHeight(), 16);
        Assertions.assertEquals(tileSet.getTileWidth(), 16);
        Assertions.assertEquals(tileSet.getTileCount(), 4);
        Assertions.assertEquals(tileSet.getColumns(), 2);
    }
}