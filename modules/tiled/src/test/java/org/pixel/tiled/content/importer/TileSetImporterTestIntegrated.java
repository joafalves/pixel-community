package org.pixel.tiled.content.importer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.pixel.content.ContentManager;
import org.pixel.core.PixelWindow;
import org.pixel.core.WindowSettings;
import org.pixel.tiled.content.TileSet;

public class TileSetImporterTestIntegrated {
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