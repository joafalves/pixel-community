package org.pixel.tiled.view;

import org.junit.jupiter.api.Test;
import org.pixel.commons.DeltaTime;
import org.pixel.content.ContentManager;
import org.pixel.core.Camera2D;
import org.pixel.core.PixelWindow;
import org.pixel.core.WindowSettings;
import org.pixel.graphics.render.BlendMode;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.keyboard.KeyboardKey;
import org.pixel.math.Vector2;
import org.pixel.tiled.content.TileMap;
import org.pixel.tiled.content.importer.TileMapImporter;
import org.pixel.tiled.content.importer.TileSetImporter;

public class TileMapViewTestIntegrated {
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
            gameCamera.setOrigin(new Vector2(0.5f, 0.5f));
            //gameCamera.translate(500, 180);
            gameCamera.setZoom(2f);

            TileMapImporter importer = new TileMapImporter();
            TileSetImporter tileSetImporter = new TileSetImporter();
            String tmxFileName = "rotation2.tmx";

            ContentManager contentManager = new ContentManager();
            contentManager.addContentImporter(importer);
            contentManager.addContentImporter(tileSetImporter);

            tileMap = contentManager.load(tmxFileName, TileMap.class);
            tileMapView = new TileMapView();
            spriteBatch = new SpriteBatch();
        }

        @Override
        public void update(DeltaTime delta) {
            super.update(delta);
            int speed = 50;

            // Keyboard direct state access for org.pixel.input detection:
            if (Keyboard.isKeyDown(KeyboardKey.UP) || Keyboard.isKeyDown(KeyboardKey.W)) {
                gameCamera.translate(0,-speed * delta.getElapsed()); // translate camera vertically
            } else if (Keyboard.isKeyDown(KeyboardKey.DOWN) || Keyboard.isKeyDown(KeyboardKey.S)) {
                gameCamera.translate(0,speed * delta.getElapsed()); // translate camera vertically
            }

            if (Keyboard.isKeyDown(KeyboardKey.LEFT)) {
                gameCamera.translate(-speed * delta.getElapsed(), 0);
            } else if (Keyboard.isKeyDown(KeyboardKey.RIGHT)) {
                gameCamera.translate(speed * delta.getElapsed(), 0);
            }

            if(Keyboard.isKeyDown(KeyboardKey.P)) {
                gameCamera.setZoom(gameCamera.getZoom()+0.1f);
            } else if (Keyboard.isKeyDown(KeyboardKey.M)) {
                gameCamera.setZoom(gameCamera.getZoom()-0.1f);
            }
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