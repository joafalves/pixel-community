package org.pixel.tiled.view;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.pixel.commons.DeltaTime;
import org.pixel.content.ContentManager;
import org.pixel.core.Camera2D;
import org.pixel.core.PixelWindow;
import org.pixel.core.WindowSettings;
import org.pixel.graphics.Color;
import org.pixel.graphics.render.BlendMode;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;
import org.pixel.tiled.content.TileMap;
import org.pixel.tiled.content.importer.TileMapImporter;
import org.pixel.tiled.content.importer.TileMapImporterTest;
import org.pixel.tiled.content.importer.TileSetImporter;

import static org.junit.jupiter.api.Assertions.*;

public class TileMapViewTest {
    public static class MockWindow extends PixelWindow {

        /**
         * Constructor
         *
         * @param settings
         */

        TileMap tileMap;
        TileMapView tileMapView;
        SpriteBatch spriteBatch;
        protected final Camera2D gameCamera;

        public MockWindow(WindowSettings settings) {
            super(settings);

            gameCamera = new Camera2D(this);
        }

        @Override
        public void load() {
            gameCamera.setOrigin(new Vector2(-1.8f, -0.6f));
            gameCamera.setZoom(3f);

            TileMapImporter importer = new TileMapImporter();
            TileSetImporter tileSetImporter = new TileSetImporter();
            String tmxFileName = "untitled.tmx";

            ContentManager contentManager = new ContentManager();
            contentManager.addContentImporter(importer);
            contentManager.addContentImporter(tileSetImporter);

            tileMap = contentManager.load(tmxFileName, TileMap.class);
            tileMapView = new TileMapView();
            spriteBatch = new SpriteBatch();

        }

        @Override
        public void draw(DeltaTime delta) {
            spriteBatch.begin(gameCamera.getViewMatrix(), BlendMode.NORMAL_BLEND);

            tileMapView.draw(spriteBatch, tileMap);

            spriteBatch.end();
        }
    }

    @Test
    public void processCase1Integrated() {
        WindowSettings settings = new WindowSettings(600, 600);
        settings.setDebugMode(true);

        PixelWindow pixelWindow = new MockWindow(settings);
        pixelWindow.start();
    }
}