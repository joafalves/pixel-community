package org.pixel.tiled.content.importer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.pixel.content.ContentManager;
import org.pixel.core.PixelWindow;
import org.pixel.core.WindowSettings;
import org.pixel.tiled.content.TileMap;

public class TileMapImporterTestIntegrated {

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
            TileMapImporter importer = new TileMapImporter();
            TileSetImporter tileSetImporter = new TileSetImporter();
            String tmxFileName = "untitled.tmx";

            ContentManager contentManager = new ContentManager();
            contentManager.addContentImporter(importer);
            contentManager.addContentImporter(tileSetImporter);

            TileMap tileMap = contentManager.load(tmxFileName, TileMap.class);

            Assertions.assertEquals(25, tileMap.getHeight());
            Assertions.assertEquals(51, tileMap.getWidth());
            Assertions.assertEquals(4, tileMap.getTileSets().get(0).getTileCount());
            Assertions.assertEquals(2, tileMap.getTileSets().get(0).getColumns());
            Assertions.assertEquals(16, tileMap.getTileSets().get(0).getTileWidth());
            Assertions.assertEquals(16, tileMap.getTileSets().get(0).getTileHeight());
            Assertions.assertEquals(1, tileMap.getTileSets().get(0).getFirstGId());
            Assertions.assertEquals(5, tileMap.getTileSets().get(1).getFirstGId());
            Assertions.assertEquals(25, tileMap.getLayers().get(0).getHeight());
            Assertions.assertEquals(51, tileMap.getLayers().get(0).getWidth());
            Assertions.assertEquals(0, tileMap.getLayers().get(0).getOffsetX());
            Assertions.assertEquals(0, tileMap.getLayers().get(0).getOffsetY());
            Assertions.assertEquals(-1, tileMap.getLayers().get(1).getOffsetX());
            Assertions.assertEquals(7.5, tileMap.getLayers().get(1).getOffsetY());

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