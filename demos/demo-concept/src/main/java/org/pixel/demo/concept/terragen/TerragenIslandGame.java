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
import org.pixel.math.MathHelper;

public class TerragenIslandGame extends PixelWindow {

    private final Logger log = LoggerFactory.getLogger(TerragenGame.class);

    private final static int SCREEN_SIZE = 1024;
    private final static int TILE_SIZE = 8;
    private final static int COLUMNS = SCREEN_SIZE / TILE_SIZE;
    private final static int ROWS = SCREEN_SIZE / TILE_SIZE;
    private final static double SCALE = 2.5f;
    private final static double BASE_HEIGHT_SHIFT = 0.3f;

    private Color[][] colorMap;
    private Color[][] heightMap;
    private PrimitiveBatch primitiveBatch;
    private Camera2D gameCamera;
    private TitleFpsCounter fpsCounter;
    private long seed;
    private boolean baseHeightModeToggle = false;
    private boolean showHeightOnly = false;

    /**
     * Constructor.
     *
     * @param settings The settings to use.
     */
    public TerragenIslandGame(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        primitiveBatch = new PrimitiveBatch(768);
        gameCamera = new Camera2D(this);
        gameCamera.setOrigin(0);
        fpsCounter = new TitleFpsCounter(this, "Press R to reset seed");
        seed = ThreadLocalRandom.current().nextLong();

        setBackgroundColor(Color.BLACK);

        final var startNanoTimestamp = System.nanoTime();
        updateColorMap();
        final var elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanoTimestamp);
        final var totalTiles = COLUMNS * ROWS;
        log.info("Noise map initialized in {}ms with seed '{}' (tiles: {}).", elapsed, seed, totalTiles);
    }

    private void updateColorMap() {
        var terrainMap = new Terrain[]{
                Terrain.builder().color(new Color(0x003EB2ff)).minHeight(-1f).build(),      // deep water
                Terrain.builder().color(new Color(0x084BB5ff)).minHeight(-0.8f).build(),      // mid water
                Terrain.builder().color(new Color(0x0952C6ff)).minHeight(-0.4f).build(),    // shore water
                Terrain.builder().color(new Color(0xA49463ff)).minHeight(-0.2f).build(),    // near water
                Terrain.builder().color(new Color(0x867645ff)).minHeight(0.20f).build(),    // transition
                Terrain.builder().color(new Color(0x3C6114ff)).minHeight(0.30f).build(),    // grass
                Terrain.builder().color(new Color(0x5A7F32ff)).minHeight(0.45f).build(),    // grass 2
                Terrain.builder().color(new Color(0x8C8E7Bff)).minHeight(0.60f).build(),    // mountain base
                Terrain.builder().color(new Color(0xA0A28Fff)).minHeight(0.70f).build(),    // mountain mid
                Terrain.builder().color(new Color(0xC7C7C7ff)).minHeight(0.90f).build(),    // mountain top
                Terrain.builder().color(new Color(0xCFCFCFff)).minHeight(0.95f).build(),    // mountain top
        };

        colorMap = new Color[COLUMNS][ROWS];
        heightMap = new Color[COLUMNS][ROWS];
        for (int x = 0; x < COLUMNS; x++) {
            for (int y = 0; y < ROWS; y++) {
                var baseHeight = 0.0f;
                if (baseHeightModeToggle) {
                    final var centerDistX = MathHelper.abs(x - (COLUMNS / 2f));
                    final var centerDistY = MathHelper.abs(y - (ROWS / 2f));
                    baseHeight = MathHelper.max(centerDistX, centerDistY) / (COLUMNS / 2f) * 2f;
                } else {
                    baseHeight = MathHelper.distance(x, y, COLUMNS / 2f, ROWS / 2f) / (COLUMNS / 2f) * 2;
                }
                baseHeight -= BASE_HEIGHT_SHIFT;

                var height =  (OpenSimplexNoise.noise2(seed,
                        x / (double) COLUMNS * SCALE,
                        y / (double) ROWS * SCALE)) - baseHeight;
                height = MathHelper.clamp(height, -1.0f, 1f);

                for (int i = terrainMap.length - 1; i >= 0; i--) {
                    if (height >= terrainMap[i].getMinHeight()) {
                        colorMap[x][y] = terrainMap[i].getColor();
                        break;
                    }
                }
                heightMap[x][y] = new Color(Color.WHITE);
                heightMap[x][y].setAlpha((height + 1f) / 2f);
            }
        }
    }

    @Override
    public void update(DeltaTime delta) {
        fpsCounter.update(delta);

        if (Keyboard.isKeyPressed(KeyboardKey.R)) {
            seed = ThreadLocalRandom.current().nextLong();
            updateColorMap();

        } else if (Keyboard.isKeyPressed(KeyboardKey.T)) {
            baseHeightModeToggle = !baseHeightModeToggle;
            updateColorMap();
        } else if (Keyboard.isKeyPressed(KeyboardKey.H)) {
            showHeightOnly = !showHeightOnly;
        }
    }

    @Override
    public void draw(DeltaTime delta) {
        primitiveBatch.begin(gameCamera.getViewMatrix(), PrimitiveType.TRIANGLES);

        for (int x = 0; x < COLUMNS; x++) {
            for (int y = 0; y < ROWS; y++) {
                var color = showHeightOnly ? heightMap[x][y] : colorMap[x][y];
                // TOP LEFT TRIANGLE
                primitiveBatch.draw(x * TILE_SIZE, y * TILE_SIZE, color);
                primitiveBatch.draw(x * TILE_SIZE + TILE_SIZE, y * TILE_SIZE, color);
                primitiveBatch.draw(x * TILE_SIZE, y * TILE_SIZE + TILE_SIZE, color);

                // BOTTOM RIGHT TRIANGLE
                primitiveBatch.draw(x * TILE_SIZE, y * TILE_SIZE + TILE_SIZE, color);
                primitiveBatch.draw(x * TILE_SIZE + TILE_SIZE, y * TILE_SIZE, color);
                primitiveBatch.draw(x * TILE_SIZE + TILE_SIZE, y * TILE_SIZE + TILE_SIZE, color);
            }
        }

        primitiveBatch.end();
    }

    @Override
    public void dispose() {
        primitiveBatch.dispose();
    }

    public static void main(String[] args) {
        WindowSettings settings = new WindowSettings(SCREEN_SIZE, SCREEN_SIZE);
        settings.setWindowResizable(true);
        settings.setMultisampling(2);
        settings.setVsync(false);
        settings.setDebugMode(false);
        settings.setWindowWidth(SCREEN_SIZE);
        settings.setWindowHeight(SCREEN_SIZE);
        settings.setIdleThrottle(false);

        PixelWindow window = new TerragenIslandGame(settings);
        window.start();
    }
}
