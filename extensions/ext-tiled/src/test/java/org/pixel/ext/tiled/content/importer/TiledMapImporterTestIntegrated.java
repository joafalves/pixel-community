package org.pixel.ext.tiled.content.importer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.pixel.content.ContentManager;
import org.pixel.core.GameWindow;
import org.pixel.core.WindowSettings;
import org.pixel.ext.tiled.content.TiledMap;

public class TiledMapImporterTestIntegrated {

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
            TiledMapImporter importer = new TiledMapImporter();
            TiledTileSetImporter tileSetImporter = new TiledTileSetImporter();
            String tmxFileName = "untitled.tmx";

            ContentManager contentManager = new ContentManager();
            contentManager.addContentImporter(importer);
            contentManager.addContentImporter(tileSetImporter);

            TiledMap tileMap = contentManager.load(tmxFileName, TiledMap.class);

            Assertions.assertEquals(25, tileMap.getHeight());
            Assertions.assertEquals(51, tileMap.getWidth());
            Assertions.assertEquals(4, tileMap.getTileSets().get(0).getTileCount());
            Assertions.assertEquals(2, tileMap.getTileSets().get(0).getColumns());
            Assertions.assertEquals(16, tileMap.getTileSets().get(0).getTileWidth());
            Assertions.assertEquals(16, tileMap.getTileSets().get(0).getTileHeight());
            Assertions.assertEquals(1, tileMap.getTileSets().get(0).getFirstGId());
            Assertions.assertEquals(5, tileMap.getTileSets().get(1).getFirstGId());
            Assertions.assertEquals(0, tileMap.getLayers().get(0).getOffsetX());
            Assertions.assertEquals(0, tileMap.getLayers().get(0).getOffsetY());
            Assertions.assertEquals(7.08, tileMap.getLayers().get(1).getOffsetX(), 0.01);
            Assertions.assertEquals(2.04, tileMap.getLayers().get(1).getOffsetY(), 0.01);

            close();
        }
    }
}