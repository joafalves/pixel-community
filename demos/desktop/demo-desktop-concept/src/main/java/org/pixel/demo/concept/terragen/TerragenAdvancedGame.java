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

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.HashMap;
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
import org.pixel.graphics.Camera2D;
import org.pixel.graphics.GameWindowSettings;
import org.pixel.graphics.GameWindow;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.keyboard.KeyboardKey;
import org.pixel.math.MathHelper;
import org.pixel.math.Rectangle;

public class TerragenAdvancedGame extends GameWindow {

    private final Logger log = LoggerFactory.getLogger(TerragenAdvancedGame.class);

    private final static int SCREEN_WIDTH = 1280;
    private final static int SCREEN_HEIGHT = 720;
    private final static int COLUMNS = SCREEN_WIDTH;
    private final static int ROWS = SCREEN_HEIGHT;

    private final ByteBuffer colorData = BufferUtils.createByteBuffer(COLUMNS * ROWS * 4);
    private final ByteBuffer heightMapData = BufferUtils.createByteBuffer(COLUMNS * ROWS * 4);

    private Texture colorTexture;
    private Texture heightMapTexture;
    private SpriteBatch spriteBatch;
    private Camera2D gameCamera;
    private long seed;
    private boolean showHeightOnly = false;
    private int maxElevation = 32;
    private int minElevation = 1;
    private double amplitude = maxElevation / 2.0f;
    private double frequency = 0.003906;
    private int octaves = 6;
    private double lacunarity = 2.1;
    private double persistence = 0.5;
    private HashMap<Integer, Color> colorMap;

    /**
     * Constructor.
     *
     * @param settings The settings to use.
     */
    public TerragenAdvancedGame(GameWindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        spriteBatch = ServiceProvider.create(SpriteBatch.class);
        gameCamera = new Camera2D(this);
        gameCamera.setOrigin(0);
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

    private void generateColorMap() {
        colorMap = new HashMap<>();
        for (int i = 1; i <= maxElevation; i++) {
            var elevation = i / (float) maxElevation;
            Color color;
            if (elevation > 0.90f) {
                color = new Color(0xCFCFCFff);
            } else if (elevation > 0.85f) {
                color = new Color(0xC7C7C7ff);
            } else if (elevation > 0.80f) {
                color = new Color(0xA0A28Fff);
            } else if (elevation > 0.70f) {
                color = new Color(0x8C8E7Bff);
            } else if (elevation > 0.55f) {
                color = new Color(0x5A7F32ff);
            } else if (elevation > 0.40f) {
                color = new Color(0x3C6114ff);
            } else if (elevation > 0.30f) {
                color = new Color(0x867645ff);
            } else if (elevation > 0.20f) {
                color = new Color(0xA49463ff);
            } else if (elevation > 0.15f) {
                color = new Color(0x0952C6ff);
            } else if (elevation > 0.07f) {
                color = new Color(0x084BB5ff);
            } else {
                color = new Color(0x003EB2ff);
            }
            colorMap.put(i, color);
        }
    }

    private double noise(double x, double y) {
        return (OpenSimplexNoise.noise2(seed, x, y));
    }

    private void generateTextures() {
        generateColorMap();
        heightMapData.clear();
        colorData.clear();

        var distributionMap = new HashMap<Integer, Long>();
        var min = 100f;
        var max = -100f;
        for (int y = 0; y < SCREEN_HEIGHT; y++) {
            for (int x = 0; x < SCREEN_WIDTH; x++) {
                var elevation = amplitude;
                var tmpFrequency = frequency;
                var tmpAmplitude = amplitude;
                for (int o = 0; o < octaves; o++) {
                    double nx = x * tmpFrequency; // + offsetX
                    double ny = y * tmpFrequency; // + offsetY
                    elevation += noise(nx, ny) * tmpAmplitude;
                    tmpFrequency *= lacunarity;
                    tmpAmplitude *= persistence;
                }
                if (elevation > max) {
                    max = (float) elevation;
                }
                if (elevation < min) {
                    min = (float) elevation;
                }
                elevation = MathHelper.clamp(MathHelper.round(elevation), minElevation, maxElevation);
                distributionMap.putIfAbsent((int) elevation, 1L);
                distributionMap.computeIfPresent((int) elevation, (key, count) -> count + 1);

                var color = colorMap.get((int) elevation);
                putColor(colorData, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
                putColor(heightMapData, 1, 1, 1, (float) (elevation / maxElevation));
            }
        }

        log.info("Distribution:");
        distributionMap.forEach((key, count) ->
                log.info("{} -> {}", key, count));
        log.info("Elevation (min: {}, max {})", new BigDecimal(min).toPlainString(),
                new BigDecimal(max).toPlainString());

        assignBufferToTexture(colorData, colorTexture);
        assignBufferToTexture(heightMapData, heightMapTexture);
        updateStatus();
    }

    private void generateColor(ByteBuffer buffer, double elevation) {
        if (elevation <= 4) {
            putColor(buffer, 0, 0, MathHelper.linearInterpolation(0.0f, 1f, (float) (elevation / 4f)), 1);

        } else if (elevation <= 16) {
            putColor(buffer, 0, MathHelper.linearInterpolation(0.0f, 1f, (float) (elevation - 4 / 12f)), 0, 1);

        } else {
            putColor(buffer, 1, 1, 1, (float) (elevation / maxElevation));
        }
    }

    private void putColor(ByteBuffer buffer, float r, float g, float b, float a) {
        buffer.put((byte) (r * 255));
        buffer.put((byte) (g * 255));
        buffer.put((byte) (b * 255));
        buffer.put((byte) (a * 255));
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
        if (Keyboard.isKeyPressed(KeyboardKey.R)) {
            seed = ThreadLocalRandom.current().nextLong();
            generateTextures();
        } else if (Keyboard.isKeyPressed(KeyboardKey.H)) {
            showHeightOnly = !showHeightOnly;
        }

        if (Keyboard.isKeyDown(KeyboardKey.F)) {
            if (Keyboard.isKeyPressed(KeyboardKey.KP_ADD)) {
                frequency *= 2.0;
                generateTextures();
            } else if (Keyboard.isKeyPressed(KeyboardKey.KP_SUBTRACT)) {
                frequency /= 2.0;
                generateTextures();
            }
        } else if (Keyboard.isKeyDown(KeyboardKey.O)) {
            if (Keyboard.isKeyPressed(KeyboardKey.KP_ADD)) {
                octaves += 1;
                generateTextures();
            } else if (Keyboard.isKeyPressed(KeyboardKey.KP_SUBTRACT)) {
                octaves -= 1;
                generateTextures();
            }
        } else if (Keyboard.isKeyDown(KeyboardKey.L)) {
            if (Keyboard.isKeyPressed(KeyboardKey.KP_ADD)) {
                lacunarity += .1;
                generateTextures();
            } else if (Keyboard.isKeyPressed(KeyboardKey.KP_SUBTRACT)) {
                lacunarity -= .1;
                generateTextures();
            }
        } else if (Keyboard.isKeyDown(KeyboardKey.P)) {
            if (Keyboard.isKeyPressed(KeyboardKey.KP_ADD)) {
                persistence += .1;
                generateTextures();
            } else if (Keyboard.isKeyPressed(KeyboardKey.KP_SUBTRACT)) {
                persistence -= .1;
                generateTextures();
            }
        } else if (Keyboard.isKeyDown(KeyboardKey.E)) {
            if (Keyboard.isKeyPressed(KeyboardKey.KP_ADD)) {
                maxElevation *= 2;
                amplitude = maxElevation / 2.0;
                generateTextures();
            } else if (Keyboard.isKeyPressed(KeyboardKey.KP_SUBTRACT)) {
                maxElevation /= 2;
                amplitude = maxElevation / 2.0;
                generateTextures();
            }
        }
    }

    private void updateStatus() {
        setWindowTitle(
                String.format(
                        "Max [E]lev: %d | Amp: %f | [F]requency: %f | [O]ctaves: %d | [L]acunarity: %f | [P]ersistence: %f",
                        maxElevation, amplitude, frequency, octaves, lacunarity, persistence));
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
        var settings = new GameWindowSettings(SCREEN_WIDTH, SCREEN_HEIGHT);
        settings.setWindowResizable(true);
        settings.setMultisampling(2);
        settings.setVsync(true);
        settings.setDebugMode(false);
        settings.setWindowWidth(SCREEN_WIDTH);
        settings.setWindowHeight(SCREEN_HEIGHT);
        settings.setIdleThrottle(false);

        var window = new TerragenAdvancedGame(settings);
        window.start();
    }
}
