package org.pixel.tiled.content.importer;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.lwjgl.system.CallbackI;
import org.mockito.Mockito;
import org.pixel.content.ContentImporter;
import org.pixel.content.ContentManager;
import org.pixel.content.ImportContext;
import org.pixel.content.Texture;
import org.pixel.core.PixelWindow;
import org.pixel.core.WindowSettings;
import org.pixel.tiled.content.TileMap;
import org.pixel.tiled.content.TileSet;
import org.w3c.dom.Text;

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

        Assertions.assertEquals(tileSet.getTileHeight(), 16);
        Assertions.assertEquals(tileSet.getTileWidth(), 16);
        Assertions.assertEquals(tileSet.getTileCount(), 4);
        Assertions.assertEquals(tileSet.getColumns(), 2);
        Assertions.assertSame(tileSet.getTexture(), texture);
    }

    public class MockWindow extends PixelWindow {

        /**
         * Constructor
         *
         * @param settings
         */

        public MockWindow(WindowSettings settings) {
            super(settings);
        }

        @Override
        @Test
        public void load() {
            TileSetImporter importer = new TileSetImporter();
            String tsxFileName = "Tileset.tsx";

            ContentManager contentManager = new ContentManager();
            contentManager.addContentImporter(importer);

            TileSet tileSet = contentManager.load(tsxFileName, TileSet.class);

            Assertions.assertEquals(tileSet.getTileHeight(), 16);
            Assertions.assertEquals(tileSet.getTileWidth(), 16);
            Assertions.assertEquals(tileSet.getTileCount(), 4);
            Assertions.assertEquals(tileSet.getColumns(), 2);
            Assertions.assertEquals(tileSet.getTexture().getHeight(), 32);
            Assertions.assertEquals(tileSet.getTexture().getWidth(), 32);

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