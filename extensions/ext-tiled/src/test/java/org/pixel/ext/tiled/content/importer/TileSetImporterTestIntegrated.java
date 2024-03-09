package org.pixel.ext.tiled.content.importer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.pixel.content.ContentManager;
import org.pixel.core.GameWindow;
import org.pixel.core.WindowSettings;
import org.pixel.ext.tiled.content.TiledTileSet;

public class TileSetImporterTestIntegrated {
    @Test
    public void processCase1Integrated() {
        WindowSettings settings = new WindowSettings(1, 1);
        settings.setDebugMode(true);

        GameWindow pixelWindow = new MockWindow(settings);
        pixelWindow.start();
    }

    public static class MockWindow extends GameWindow {

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
            TiledTileSetImporter importer = new TiledTileSetImporter();
            String tsxFileName = "Tileset.tsx";

            ContentManager contentManager = new ContentManager();
            contentManager.addContentImporter(importer);

            TiledTileSet tileSet = contentManager.load(tsxFileName, TiledTileSet.class);

            Assertions.assertEquals(16, tileSet.getTileHeight());
            Assertions.assertEquals(16, tileSet.getTileWidth());
            Assertions.assertEquals(4, tileSet.getTileCount());
            Assertions.assertEquals(2, tileSet.getColumns());
            Assertions.assertEquals(32, tileSet.getTexture().getHeight());
            Assertions.assertEquals(32, tileSet.getTexture().getWidth());

            close();
        }
    }
}