package org.pixel.demo.concept.terragen;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.core.Camera2D;
import org.pixel.core.GameWindow;
import org.pixel.core.WindowSettings;
import org.pixel.demo.concept.commons.FpsCounter;
import org.pixel.graphics.Color;
import org.pixel.graphics.PrimitiveType;
import org.pixel.graphics.render.PrimitiveBatch;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.keyboard.KeyboardKey;
import org.pixel.math.MathHelper;

public class TerragenGame extends GameWindow {

    private final Logger log = LoggerFactory.getLogger(TerragenGame.class);

    private final static int SCREEN_WIDTH = 800;
    private final static int SCREEN_HEIGHT = 800;
    private final static int TILE_SIZE = 10;
    private final static int COLUMNS = SCREEN_WIDTH / TILE_SIZE;
    private final static int ROWS = SCREEN_HEIGHT / TILE_SIZE;
    private final static double SPECTRUM_SCALE = 1;
    private final static double FREQUENCY_SCALE = 1.25;
    private final static double EXP = 1d;

    private Color[][] colorMap;
    private PrimitiveBatch primitiveBatch;
    private Camera2D gameCamera;
    private FpsCounter fpsCounter;
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
        fpsCounter = new FpsCounter(this, "Press R to reset seed");
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

    private double noise(double x, double y) {
        return (OpenSimplexNoise.noise2(seed, x, y) + 1.0) / 2.0;
    }

    private void updateColorMap() {
        var terrainMap = new Terrain[]{
                Terrain.builder().color(new Color(0x003EB2ff)).minHeight(0).build(),      // deep water
                Terrain.builder().color(new Color(0x084BB5ff)).minHeight(0.07f).build(),      // mid water
                Terrain.builder().color(new Color(0x0952C6ff)).minHeight(0.15f).build(),    // shore water
                Terrain.builder().color(new Color(0xA49463ff)).minHeight(0.20f).build(),    // near water
                Terrain.builder().color(new Color(0x867645ff)).minHeight(0.30f).build(),    // transition
                Terrain.builder().color(new Color(0x3C6114ff)).minHeight(0.40f).build(),    // grass
                Terrain.builder().color(new Color(0x5A7F32ff)).minHeight(0.55f).build(),    // grass 2
                Terrain.builder().color(new Color(0x8C8E7Bff)).minHeight(0.70f).build(),    // mountain base
                Terrain.builder().color(new Color(0xA0A28Fff)).minHeight(0.80f).build(),    // mountain mid
                Terrain.builder().color(new Color(0xC7C7C7ff)).minHeight(0.85f).build(),    // mountain top
                Terrain.builder().color(new Color(0xCFCFCFff)).minHeight(0.90f).build(),    // mountain top
        };

        colorMap = new Color[COLUMNS][ROWS];

        // SIMPLE:
        /*for (int x = 0; x < COLUMNS; x++) {
            for (int y = 0; y < ROWS; y++) {
                var height = (OpenSimplexNoise.noise2(seed,
                        (x + px) / (double) COLUMNS * SCALE,
                        (y + py) / (double) ROWS * SCALE));
                height = MathHelper.clamp(height, -1.0f, 1f);

                for (int i = terrainMap.length - 1; i >= 0; i--) {
                    if (height >= terrainMap[i].getMinHeight()) {
                        colorMap[x][y] = terrainMap[i].getColor();
                        break;
                    }
                }
            }
        }*/

        final double[] spectrum = new double[]{SPECTRUM_SCALE, SPECTRUM_SCALE / 2, SPECTRUM_SCALE / 4,
                SPECTRUM_SCALE / 8, SPECTRUM_SCALE
                / 16, SPECTRUM_SCALE / 32};
        final double[] amplitudes = new double[spectrum.length];
        final double[] frequencies = new double[spectrum.length];
        double effectiveScale = 0;
        for (int octave = 0, exponent = 1; octave < spectrum.length; octave++, exponent *= 2) {
            effectiveScale += spectrum[octave];
            amplitudes[octave] = spectrum[octave];
            frequencies[octave] = exponent * FREQUENCY_SCALE;
        }
        for (int octave = 0; octave < spectrum.length; octave++) {
            amplitudes[octave] = amplitudes[octave] / effectiveScale;
        }

        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLUMNS; x++) {
                double nx = x / (double) COLUMNS * (SCREEN_WIDTH / (float) SCREEN_HEIGHT), ny = y / (double) ROWS;
                double e = 0;
                for (int i = 0; i < amplitudes.length; i++) {
                    e += amplitudes[i] * noise((nx + px) * frequencies[i], (ny + py) * frequencies[i]);
                }
                float height = (float) e;

                if (EXP != 1.0) {
                    height = MathHelper.pow(height, 2.0f); // redistribution
                }

                height = MathHelper.clamp(height, 0f, 1f);

                for (int i = terrainMap.length - 1; i >= 0; i--) {
                    if (height >= terrainMap[i].getMinHeight()) {
                        colorMap[x][y] = terrainMap[i].getColor();
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void update(DeltaTime delta) {
        fpsCounter.update(delta);

        if (!Keyboard.isKeyDown(KeyboardKey.P)) {
            px += delta.getElapsed() * .1f;
            py += delta.getElapsed() * .1f;
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
        settings.setVsync(true);
        settings.setDebugMode(false);
        settings.setWindowWidth(SCREEN_WIDTH);
        settings.setWindowHeight(SCREEN_HEIGHT);
        settings.setIdleThrottle(false);

        GameWindow window = new TerragenGame(settings);
        window.start();
    }
}
