package org.pixel.tiled.content.importer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pixel.content.ContentManager;
import org.pixel.content.ImportContext;
import org.pixel.content.Texture;
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
        ContentManager contentManager = Mockito.mock(ContentManager.class);
        Texture texture = Mockito.mock(Texture.class);

        Mockito.when(contentManager.load(Mockito.anyString(), Mockito.eq(Texture.class))).thenReturn(texture);

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
}