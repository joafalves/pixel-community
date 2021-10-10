package org.pixel.tiled.content.importer;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.pixel.content.ContentManager;
import org.pixel.content.ImportContext;
import org.pixel.content.Texture;
import org.pixel.core.PixelWindow;
import org.pixel.core.WindowSettings;
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

    public static class MockWindow extends PixelWindow {

        /**
         * Constructor
         *
         * @param settings
         */

        public MockWindow(WindowSettings settings) {
            super(settings);
        }

        @Override
        public void load() {
            TileSetImporter importer = new TileSetImporter();
            String tsxFileName = "Tileset.tsx";

            ContentManager contentManager = new ContentManager();
            contentManager.addContentImporter(importer);

            TileSet tileSet = contentManager.load(tsxFileName, TileSet.class);

            Assertions.assertEquals(16, tileSet.getTileHeight());
            Assertions.assertEquals(16, tileSet.getTileWidth());
            Assertions.assertEquals(4, tileSet.getTileCount());
            Assertions.assertEquals(2, tileSet.getColumns());
            Assertions.assertEquals(32, tileSet.getTexture().getHeight());
            Assertions.assertEquals(32, tileSet.getTexture().getWidth());

            close();
        }
    }

    @Test
    public void processCase1Integrated() {
        WindowSettings settings = new WindowSettings(1, 1);
        settings.setDebugMode(true);

        PixelWindow pixelWindow = new MockWindow(settings);
        pixelWindow.start();
    }
}