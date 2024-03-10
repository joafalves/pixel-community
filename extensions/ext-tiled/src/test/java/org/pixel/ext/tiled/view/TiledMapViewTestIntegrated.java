package org.pixel.ext.tiled.view;

import org.junit.jupiter.api.Test;
import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.content.ContentManager;
import org.pixel.content.importer.settings.ContentImporterSettings;
import org.pixel.core.Camera2D;
import org.pixel.ext.tiled.content.TiledMap;
import org.pixel.ext.tiled.content.importer.TiledMapImporter;
import org.pixel.ext.tiled.content.importer.TiledMapImporterSettings;
import org.pixel.ext.tiled.content.importer.TiledTileSetImporter;
import org.pixel.graphics.DesktopGameSettings;
import org.pixel.graphics.DesktopGameWindow;
import org.pixel.graphics.render.BlendMode;
import org.pixel.graphics.render.NvgRenderEngine;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.keyboard.KeyboardKey;
import org.pixel.math.Vector2;

public class TiledMapViewTestIntegrated {
    @Test
    public void processCase1Integrated() {
        var settings = new DesktopGameSettings(600, 600);
        settings.setDebugMode(true);

        var pixelWindow = new MockWindow(settings);
        pixelWindow.start();
    }

    public static class MockWindow extends DesktopGameWindow {

        protected final Camera2D gameCamera;
        /**
         * Constructor
         *
         * @param settings
         */

        TiledMap tileMap;
        TiledMapView tileMapView;
        SpriteBatch spriteBatch;
        NvgRenderEngine nvg;
        Color fillColor;
        float posX, posY;

        public MockWindow(DesktopGameSettings settings) {
            super(settings);

            gameCamera = new Camera2D(this);
        }

        @Override
        public void load() {
            nvg = new NvgRenderEngine(getSettings().getWindowWidth(), getSettings().getWindowHeight());
            fillColor = Color.BLACK;
            gameCamera.setOrigin(new Vector2(0f, 0f));
            //gameCamera.translate(500, 180);
            gameCamera.setZoom(3f);
            posX = 0;
            posY = 0;

            TiledMapImporter importer = new TiledMapImporter();
            TiledTileSetImporter tileSetImporter = new TiledTileSetImporter();
            String tmxFileName = "rotation2.tmx";

            ContentManager contentManager = new ContentManager();
            contentManager.addContentImporter(importer);
            contentManager.addContentImporter(tileSetImporter);

            ContentImporterSettings tileMapImporterSettings = new TiledMapImporterSettings();

            tileMap = contentManager.load(tmxFileName, TiledMap.class, tileMapImporterSettings);
            spriteBatch = new SpriteBatch();
            tileMapView = new TiledMapView(spriteBatch, gameCamera);
        }

        @Override
        public void update(DeltaTime delta) {
            super.update(delta);
            int speed = 50;


            // Keyboard direct state access for org.pixel.input detection:
            if (Keyboard.isKeyDown(KeyboardKey.UP) || Keyboard.isKeyDown(KeyboardKey.W)) {
                posY -= speed * delta.getElapsed();
            } else if (Keyboard.isKeyDown(KeyboardKey.DOWN) || Keyboard.isKeyDown(KeyboardKey.S)) {
                posY += speed * delta.getElapsed();
            }

            if (Keyboard.isKeyDown(KeyboardKey.LEFT)) {
                posX -= speed * delta.getElapsed();
            } else if (Keyboard.isKeyDown(KeyboardKey.RIGHT)) {
                posX += speed * delta.getElapsed();
            }

            gameCamera.setPosition((float) Math.floor(posX), (float) Math.floor(posY));

            if (Keyboard.isKeyDown(KeyboardKey.P)) {
                gameCamera.setZoom(gameCamera.getZoom() + 0.1f);
            } else if (Keyboard.isKeyDown(KeyboardKey.M)) {
                gameCamera.setZoom(gameCamera.getZoom() - 0.1f);
            }
        }

        @Override
        public void draw(DeltaTime delta) {
            spriteBatch.begin(gameCamera.getViewMatrix(), BlendMode.NORMAL_BLEND);

            tileMapView.draw(tileMap, delta.getElapsedMs());

            spriteBatch.end();
        }
    }
}