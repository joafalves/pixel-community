package org.pixel.demo.tiled;

import org.pixel.commons.DeltaTime;
import org.pixel.commons.logger.ConsoleLogger;
import org.pixel.commons.logger.LogLevel;
import org.pixel.content.ContentManager;
import org.pixel.content.importer.settings.ContentImporterSettings;
import org.pixel.core.Camera2D;
import org.pixel.core.PixelWindow;
import org.pixel.core.WindowSettings;
import org.pixel.ext.tiled.content.TiledMap;
import org.pixel.ext.tiled.content.importer.TiledMapImporter;
import org.pixel.ext.tiled.content.importer.TiledMapImporterSettings;
import org.pixel.ext.tiled.content.importer.TiledTileSetImporter;
import org.pixel.ext.tiled.view.TiledMapView;
import org.pixel.graphics.Color;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.keyboard.KeyboardKey;

public class TiledDemo extends PixelWindow {

    private static final float CAMERA_SPEED = 100f;

    private SpriteBatch spriteBatch;
    private ContentManager contentManager;
    private Camera2D gameCamera;
    private static Color fillColor;
    float posX, posY;

    private TiledMap tileMap;
    private TiledMapView tileMapView;

    private TitleFpsCounter titleFpsCounter;

    /**
     * Constructor.
     *
     * @param settings The settings to use.
     */
    public TiledDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        titleFpsCounter = new TitleFpsCounter(this);
        gameCamera = new Camera2D(this);
        gameCamera.setOrigin(0.5f, 0.5f);
        gameCamera.setZoom(3f);
        posX = 8 * 16;
        posY = 5 * 16 + 1;

        TiledTileSetImporter tileSetImporter = new TiledTileSetImporter();
        TiledMapImporter importer = new TiledMapImporter();

        spriteBatch = new SpriteBatch();
        contentManager = new ContentManager();
        contentManager.addContentImporter(importer);
        contentManager.addContentImporter(tileSetImporter);

        ContentImporterSettings tileMapImporterSettings = new TiledMapImporterSettings();

        tileMap = contentManager.load("map.tmx", TiledMap.class, tileMapImporterSettings);
        tileMapView = new TiledMapView(spriteBatch, gameCamera);
        fillColor = Color.BLACK;
    }

    @Override
    public void update(DeltaTime delta) {
        // Keyboard direct state access for org.pixel.input detection:
        if (Keyboard.isKeyDown(KeyboardKey.UP) || Keyboard.isKeyDown(KeyboardKey.W)) {
            posY -= CAMERA_SPEED * delta.getElapsed();
        } else if (Keyboard.isKeyDown(KeyboardKey.DOWN) || Keyboard.isKeyDown(KeyboardKey.S)) {
            posY += CAMERA_SPEED * delta.getElapsed();
        }

        if (Keyboard.isKeyDown(KeyboardKey.LEFT)) {
            posX -= CAMERA_SPEED * delta.getElapsed();
        } else if (Keyboard.isKeyDown(KeyboardKey.RIGHT)) {
            posX += CAMERA_SPEED * delta.getElapsed();
        }

        gameCamera.setPosition((float) Math.floor(posX), (float) Math.floor(posY));

        if (Keyboard.isKeyDown(KeyboardKey.D_1)) {
            gameCamera.setZoom(1f);
        } else if (Keyboard.isKeyDown(KeyboardKey.D_2)) {
            gameCamera.setZoom(2f);
        } else if (Keyboard.isKeyDown(KeyboardKey.D_3)) {
            gameCamera.setZoom(3f);
        }

        titleFpsCounter.update(delta);
    }

    @Override
    public void draw(DeltaTime delta) {
        setBackgroundColor(fillColor);

        spriteBatch.begin(gameCamera.getViewMatrix());

        tileMapView.draw(tileMap, delta.getElapsedMs());

        spriteBatch.end();
    }

    @Override
    public void dispose() {
        super.dispose();
        spriteBatch.dispose();
        contentManager.dispose();
    }

    public static void main(String[] args) {
        final int width = 1280;
        final int height = 720;
        WindowSettings settings = new WindowSettings(width, height);
        settings.setWindowTitle("Tiled Demo");
        settings.setWindowResizable(true);
        settings.setVsync(true);
        settings.setDebugMode(false);
        settings.setWindowWidth(width);
        settings.setWindowHeight(height);

        ConsoleLogger.setLogLevel(LogLevel.TRACE);

        PixelWindow window = new TiledDemo(settings);
        window.start();
    }
}
