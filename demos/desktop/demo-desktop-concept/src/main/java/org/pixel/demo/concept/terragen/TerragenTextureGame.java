package org.pixel.demo.concept.terragen;

import static org.lwjgl.opengl.GL11C.GL_NEAREST;
import static org.lwjgl.opengl.GL11C.GL_REPEAT;
import static org.lwjgl.opengl.GL11C.GL_RGBA;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11C.glBindTexture;
import static org.lwjgl.opengl.GL11C.glGenTextures;
import static org.lwjgl.opengl.GL11C.glTexImage2D;
import static org.lwjgl.opengl.GL11C.glTexParameteri;
import static org.lwjgl.system.libc.LibCStdlib.free;

import java.nio.ByteBuffer;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.lwjgl.BufferUtils;
import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.ServiceProvider;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.content.Texture;
import org.pixel.content.opengl.GLTexture;
import org.pixel.demo.concept.commons.FpsCounter;
import org.pixel.core.Camera2D;
import org.pixel.core.WindowSettings;
import org.pixel.core.Game;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.keyboard.KeyboardKey;
import org.pixel.math.MathHelper;
import org.pixel.math.Rectangle;

public class TerragenTextureGame extends Game {

    private final Logger log = LoggerFactory.getLogger(TerragenTextureGame.class);

    private final static int SCREEN_WIDTH = 1280;
    private final static int SCREEN_HEIGHT = 720;
    private final static int COLUMNS = SCREEN_WIDTH;
    private final static int ROWS = SCREEN_HEIGHT;
    private final static double SPECTRUM_SCALE = 1;
    private final static double FREQUENCY_SCALE = 2.65;
    //private final static double EXP = 1d;

    private final ByteBuffer colorData = BufferUtils.createByteBuffer(COLUMNS * ROWS * 4);
    private final ByteBuffer heightMapData = BufferUtils.createByteBuffer(COLUMNS * ROWS * 4);

    private Texture colorTexture;
    private Texture heightMapTexture;
    private SpriteBatch spriteBatch;
    private Camera2D gameCamera;
    private FpsCounter fpsCounter;
    private long seed;
    private boolean baseHeightModeToggle = false;
    private boolean showHeightOnly = false;
    private boolean islandMode = true;
    private boolean noiseMode = false;

    /**
     * Constructor.
     *
     * @param settings The settings to use.
     */
    public TerragenTextureGame(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        spriteBatch = ServiceProvider.create(SpriteBatch.class);
        gameCamera = new Camera2D(this);
        gameCamera.setOrigin(0);
        fpsCounter = new FpsCounter(this, "Press R to reset seed");
        seed = ThreadLocalRandom.current().nextLong();
        colorTexture = new GLTexture(glGenTextures(), SCREEN_WIDTH, SCREEN_HEIGHT);
        heightMapTexture = new GLTexture(glGenTextures(), SCREEN_WIDTH, SCREEN_HEIGHT);

        setBackgroundColor(Color.BLACK);

        final var startNanoTimestamp = System.nanoTime();
        generateTextures();
        final var elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanoTimestamp);
        final var totalTiles = COLUMNS * ROWS;
        log.info("Noise map initialized in {}ms with seed '{}' (tiles: {}).", elapsed, seed, totalTiles);
    }

    private double noise(double x, double y) {
        if (noiseMode) {
            return (OpenSimplexNoise.noise2_ImproveX(seed, x, y) + 1.0) / 2.0;
        }

        return (OpenSimplexNoise.noise2(seed, x, y) + 1.0) / 2.0;
    }

    private void generateTextures() {
        var terrainMap = new Terrain[]{
                Terrain.builder().color(new Color(0x003EB2ff)).minHeight(0).build(),        // deep water
                Terrain.builder().color(new Color(0x084BB5ff)).minHeight(0.07f).build(),    // mid water
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

        final double[] spectrum = new double[] {SPECTRUM_SCALE, SPECTRUM_SCALE / 2, SPECTRUM_SCALE / 4, SPECTRUM_SCALE / 8, SPECTRUM_SCALE
                / 16, SPECTRUM_SCALE / 32 };
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

        // generate color & height maps
        float min = 1, max = 0;
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLUMNS; x++) {
                double nx = x / (double) COLUMNS * (SCREEN_WIDTH / (float) SCREEN_HEIGHT), ny = y / (double) ROWS;
                double e = 0;
                for (int i = 0; i < amplitudes.length; i++) {
                    e += amplitudes[i] * noise(nx * frequencies[i], ny * frequencies[i]);
                }
                float height = (float) e;
                if (islandMode) {
                    var heightReduction = 0.0f;
                    if (baseHeightModeToggle) {
                        final var centerDistX = MathHelper.abs(x - (COLUMNS / 2f));
                        final var centerDistY = MathHelper.abs(y - (ROWS / 2f));
                        heightReduction = MathHelper.max(centerDistX, centerDistY) / (COLUMNS / 2f);
                    } else {
                        heightReduction = MathHelper.distance(x, y, COLUMNS / 2f, ROWS / 2f) / (COLUMNS / 2f);
                    }
                    height -= heightReduction;
                }

                //if (EXP != 1.0) {
                //    height = MathHelper.pow(height, 2.0f); // redistribution
                //}

                max = MathHelper.max(height, max);
                min = MathHelper.min(height, min);
                height = MathHelper.clamp(height, 0f, 1f);

                for (int i = terrainMap.length - 1; i >= 0; i--) {
                    if (height >= terrainMap[i].getMinHeight()) {
                        putColor(colorData, terrainMap[i].getColor());
                        break;
                    }
                }

                putColor(heightMapData, new Color(1, 1, 1, height));
            }
        }

        System.out.println("Min: " + min);
        System.out.println("Max: " + max);

        assignBufferToTexture(colorData, colorTexture);
        assignBufferToTexture(heightMapData, heightMapTexture);
    }

    private void putColor(ByteBuffer buffer, Color color) {
        buffer.put((byte) (color.getRed() * 255));
        buffer.put((byte) (color.getGreen() * 255));
        buffer.put((byte) (color.getBlue() * 255));
        buffer.put((byte) (color.getAlpha() * 255));
    }

    private void assignBufferToTexture(ByteBuffer buffer, Texture texture) {
        glBindTexture(GL_TEXTURE_2D, texture.getId());
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, COLUMNS, ROWS, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer.flip());
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    @Override
    public void update(DeltaTime delta) {
        fpsCounter.update(delta);

        if (Keyboard.isKeyPressed(KeyboardKey.R)) {
            seed = ThreadLocalRandom.current().nextLong();
            generateTextures();
        } else if (Keyboard.isKeyPressed(KeyboardKey.T)) {
            baseHeightModeToggle = !baseHeightModeToggle;
            generateTextures();
        } else if (Keyboard.isKeyPressed(KeyboardKey.H)) {
            showHeightOnly = !showHeightOnly;
        } else if (Keyboard.isKeyPressed(KeyboardKey.I)) {
            islandMode = !islandMode;
            generateTextures();
        } else if (Keyboard.isKeyPressed(KeyboardKey.F)) {
            toggleFullscreen();
        } else if (Keyboard.isKeyPressed(KeyboardKey.N)) {
            noiseMode = !noiseMode;
            generateTextures();
        }
    }

    @Override
    public void draw(DeltaTime delta) {
        spriteBatch.begin(gameCamera.getViewMatrix());

        if (showHeightOnly) {
            spriteBatch.draw(heightMapTexture, new Rectangle(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT));
        } else {
            spriteBatch.draw(colorTexture, new Rectangle(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT));
        }

        spriteBatch.end();
    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
        colorTexture.dispose();
        heightMapTexture.dispose();
        free(colorData);
        free(heightMapData);
    }

    public static void main(String[] args) {
        var settings = new WindowSettings(SCREEN_WIDTH, SCREEN_HEIGHT);
        settings.setWindowResizable(true);
        settings.setMultisampling(2);
        settings.setVsync(true);
        settings.setDebugMode(false);
        settings.setWindowWidth(SCREEN_WIDTH);
        settings.setWindowHeight(SCREEN_HEIGHT);
        settings.setIdleThrottle(false);

        var window = new TerragenTextureGame(settings);
        window.start();
    }
}
