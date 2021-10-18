package org.pixel.tiled.content.importer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pixel.content.ContentManager;
import org.pixel.content.ImportContext;
import org.pixel.content.Texture;
import org.pixel.content.importer.settings.TextureImporterSettings;
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