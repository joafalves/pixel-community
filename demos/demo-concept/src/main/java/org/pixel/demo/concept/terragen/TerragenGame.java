package org.pixel.demo.concept.terragen;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.core.Camera2D;
import org.pixel.core.PixelWindow;
import org.pixel.core.WindowSettings;
import org.pixel.demo.concept.commons.TitleFpsCounter;
import org.pixel.graphics.Color;
import org.pixel.graphics.PrimitiveType;
import org.pixel.graphics.render.PrimitiveBatch;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.keyboard.KeyboardKey;

public class TerragenGame extends PixelWindow {

    private final Logger log = LoggerFactory.getLogger(TerragenGame.class);

    private final static int SCREEN_WIDTH = 640;
    private final static int SCREEN_HEIGHT = 640;
    private final static int TILE_SIZE = 16;
    private final static int COLUMNS = SCREEN_WIDTH / TILE_SIZE;
    private final static int ROWS = SCREEN_HEIGHT / TILE_SIZE;
    private final static double SCALE = 2;

    private Color[][] colorMap;
    private PrimitiveBatch primitiveBatch;
    private Camera2D gameCamera;
    private TitleFpsCounter fpsCounter;
    private long seed;
    private float px, py;

    /**
     * Constructor.
     *
     * @param settings The settings to use.
     */
    public TerragenGame(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        primitiveBatch = new PrimitiveBatch(768);
        gameCamera = new Camera2D(this);
        gameCamera.setOrigin(0);
        fpsCounter = new TitleFpsCounter(this, "Press R to reset seed");
        seed = ThreadLocalRandom.current().nextLong();
        px = 0;
        py = 0;
        setBackgroundColor(Color.BLACK);

        final var startNanoTimestamp = System.nanoTime();
        updateColorMap();
        final var elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanoTimestamp);
        final var totalTiles = COLUMNS * ROWS;
        log.info("Noise map initialized in {}ms with seed '{}' (tiles: {}).", elapsed, seed, totalTiles);
    }

    private void updateColorMap() {
        colorMap = new Color[COLUMNS][ROWS];
        for (int x = 0; x < COLUMNS; x++) {
            for (int y = 0; y < ROWS; y++) {
                // TODO: simplify the alpha interpolation
                final var noise = (OpenSimplexNoise.noise2_ImproveX(seed,
                        (x + (int) px) / (double) COLUMNS * SCALE,
                        (y + (int) py) / (double) ROWS * SCALE) + 1) / 2f;
                var alpha = 0f;
                if (noise < 0.10) { // deep water
                    colorMap[x][y] = new Color(0x2a6080ff);
                    alpha = noise / 0.10f;
                } else if (noise < 0.15) { // shore water
                    colorMap[x][y] = new Color(0x3a70A0ff);
                    alpha = noise / 0.15f - 0.10f;
                } else if (noise < 0.25) { // near water
                    colorMap[x][y] = new Color(0x12410fff);
                    alpha = noise / 0.25f - 0.15f;
                } else if (noise < 0.5) { // grass
                    colorMap[x][y] = new Color(0x32611fff);
                    alpha = noise / 0.5f - 0.25f;
                } else { // deep grass
                    colorMap[x][y] = new Color(0x22410fff);
                    alpha = noise - 0.5f;
                }
                colorMap[x][y].setAlpha(0.7f + (0.3f * alpha));
            }
        }
    }

    @Override
    public void update(DeltaTime delta) {
        fpsCounter.update(delta);

        if (!Keyboard.isKeyDown(KeyboardKey.P)) {
            px += delta.getElapsed() * 10f;
            py += delta.getElapsed() * 10f;
            updateColorMap();
        }

        if (Keyboard.isKeyPressed(KeyboardKey.R)) {
            seed = ThreadLocalRandom.current().nextLong();
        }
    }

    @Override
    public void draw(DeltaTime delta) {
        primitiveBatch.begin(gameCamera.getViewMatrix(), PrimitiveType.TRIANGLES);

        for (int x = 0; x < COLUMNS; x++) {
            for (int y = 0; y < ROWS; y++) {
                // TOP LEFT TRIANGLE
                primitiveBatch.draw(x * TILE_SIZE, y * TILE_SIZE, colorMap[x][y]);
                primitiveBatch.draw(x * TILE_SIZE + TILE_SIZE, y * TILE_SIZE, colorMap[x][y]);
                primitiveBatch.draw(x * TILE_SIZE, y * TILE_SIZE + TILE_SIZE, colorMap[x][y]);

                // BOTTOM RIGHT TRIANGLE
                primitiveBatch.draw(x * TILE_SIZE, y * TILE_SIZE + TILE_SIZE, colorMap[x][y]);
                primitiveBatch.draw(x * TILE_SIZE + TILE_SIZE, y * TILE_SIZE, colorMap[x][y]);
                primitiveBatch.draw(x * TILE_SIZE + TILE_SIZE, y * TILE_SIZE + TILE_SIZE, colorMap[x][y]);
            }
        }

        primitiveBatch.end();
    }

    @Override
    public void dispose() {
        primitiveBatch.dispose();
    }

    public static void main(String[] args) {
        WindowSettings settings = new WindowSettings(SCREEN_WIDTH, SCREEN_HEIGHT);
        settings.setWindowResizable(true);
        settings.setMultisampling(2);
        settings.setVsync(false);
        settings.setDebugMode(false);
        settings.setWindowWidth(SCREEN_WIDTH);
        settings.setWindowHeight(SCREEN_HEIGHT);
        settings.setIdleThrottle(false);

        PixelWindow window = new TerragenGame(settings);
        window.start();
    }
}
