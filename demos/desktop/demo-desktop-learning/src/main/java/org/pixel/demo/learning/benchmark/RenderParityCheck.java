package org.pixel.demo.learning.benchmark;

import org.pixel.core.DesktopWindowManager;
import org.pixel.core.DesktopLauncher;
import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.content.ContentManager;
import org.pixel.content.Font;
import org.pixel.content.Texture;
import org.pixel.content.importer.settings.FontImporterSettings;
import org.pixel.core.Camera2D;
import org.pixel.core.GameApp;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.Screenshot;
import org.pixel.graphics.render.BlendMode;
import org.pixel.graphics.render.DrawListSpriteBatch;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.graphics.render.opengl.GLSpriteBatch;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Renders the same scenes with the classic {@link GLSpriteBatch} and the draw-list {@link DrawListSpriteBatch},
 * reads both images back from the GPU and compares them pixel by pixel. Both images (and a diff) are written to
 * {@code build/parity/} so they can be inspected.
 * <p>
 * Scenes stay within what the classic batch renders correctly (at most 256 sprites per begin/end, so its per-chunk
 * depth sort is still a full sort; no newlines in text, which it ignored).
 * <p>
 * Run with: {@code ./gradlew :pixel-demo-desktop-learning:renderParity}
 */
public class RenderParityCheck extends GameApp {

    private static final Logger log = LoggerFactory.getLogger(RenderParityCheck.class);
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    /**
     * Per-channel difference allowed: the draw list stores colors as 8-bit (the classic batch as floats), which can
     * round a tint differently by one step.
     */
    private static final int TOLERANCE = 2;

    private ContentManager content;
    private SpriteBatch legacy;
    private SpriteBatch drawList;
    private Camera2D camera;
    private Texture[] textures;
    private Font font;
    private final List<Scene> scenes = new ArrayList<>();
    private boolean done;
    private int failures;

    private record Scene(String name, BlendMode blendMode, Consumer<SpriteBatch> draw) {
    }

    @Override
    public void load() {
        content = content();
        legacy = new GLSpriteBatch(graphics().getStats());
        legacy.init();
        drawList = new DrawListSpriteBatch(graphics().getRenderBackend());
        drawList.init();
        camera = new Camera2D(context());
        camera.setOrigin(0, 0);

        String[] files = {"images/blue-32x32.png", "images/green-32x32.png", "images/red-32x32.png",
                "images/earth-48x48.png", "images/char-anim-50x85.png", "images/a-char-256x256.png"};
        textures = new Texture[24];
        for (int i = 0; i < textures.length; i++) {
            textures[i] = content.load(files[i % files.length], Texture.class, null, false);
        }
        font = content.load("fonts/gidole-regular.ttf", Font.class, new FontImporterSettings(24, 1));

        scenes.add(new Scene("sprites-transforms", BlendMode.NORMAL_BLEND, this::spritesWithTransforms));
        scenes.add(new Scene("source-rects-and-areas", BlendMode.NORMAL_BLEND, this::sourceRectsAndAreas));
        scenes.add(new Scene("depth-sorted", BlendMode.NORMAL_BLEND, this::depthSorted));
        scenes.add(new Scene("24-textures", BlendMode.NORMAL_BLEND, this::manyTextures));
        scenes.add(new Scene("text-and-sprites", BlendMode.NORMAL_BLEND, this::textAndSprites));
        scenes.add(new Scene("additive", BlendMode.ADDITIVE, this::spritesWithTransforms));
        scenes.add(new Scene("multiply", BlendMode.MULTIPLY, this::spritesWithTransforms));
    }

    @Override
    public void draw(DeltaTime delta) {
        if (done) {
            return;
        }
        done = true;

        int width = DesktopWindowManager.of(context()).getWindowWidth();   // framebuffer pixels (HiDPI aware)
        int height = DesktopWindowManager.of(context()).getWindowHeight();
        try {
            Path dir = Path.of("build", "parity");
            Files.createDirectories(dir);
            for (Scene scene : scenes) {
                byte[] a = render(scene, legacy, width, height);
                byte[] b = render(scene, drawList, width, height);
                compare(scene, a, b, width, height, dir);
            }
        } catch (IOException e) {
            log.error("Unable to write parity images.", e);
            failures++;
        }

        if (failures == 0) {
            log.info("PARITY OK: {0} scenes match (within {1}/255 per channel, edge shifts allowed).", scenes.size(),
                    TOLERANCE);
        } else {
            log.error("PARITY FAILED: {0} of {1} scenes differ (see build/parity).", failures, scenes.size());
        }
        exit();
    }

    private byte[] render(Scene scene, SpriteBatch batch, int width, int height) {
        graphics().clear();
        batch.begin(camera.getViewMatrix(), scene.blendMode);
        scene.draw.accept(batch);
        batch.end();
        return graphics().readPixels(0, 0, width, height, true);
    }

    /**
     * Compare two images. A pixel that differs by more than {@link #TOLERANCE} counts as an <b>edge shift</b> when its
     * value appears (within tolerance) in the 3x3 neighbourhood of the other image, in both directions: the same
     * geometry rasterized from vertices that differ in the last float bits moves an edge by one pixel at most. Any
     * other difference is a real <b>mismatch</b> and fails the scene.
     */
    private void compare(Scene scene, byte[] a, byte[] b, int width, int height, Path dir) {
        int maxDiff = 0;
        int edgeShifts = 0;
        int mismatches = 0;
        byte[] diff = new byte[a.length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int i = (y * width + x) * 4;
                int pixelDiff = channelDiff(a, i, b, i);
                maxDiff = Math.max(maxDiff, pixelDiff);
                diff[i + 3] = (byte) 255;
                if (pixelDiff <= TOLERANCE) {
                    continue;
                }
                if (inNeighbourhood(a, i, b, x, y, width, height) && inNeighbourhood(b, i, a, x, y, width, height)) {
                    edgeShifts++;
                    diff[i + 2] = (byte) 255; // blue: edge shift
                } else {
                    mismatches++;
                    diff[i] = (byte) 255; // red: real mismatch
                    if (mismatches <= 5) {
                        log.warn("  mismatch at ({0}, {1}): legacy {2} vs drawlist {3}", x, y, rgb(a, i), rgb(b, i));
                    }
                }
            }
        }

        boolean ok = mismatches == 0;
        if (!ok) {
            failures++;
        }
        log.info("{0} {1}: max channel diff {2}, edge shifts {3}, mismatches {4}", ok ? "OK  " : "FAIL",
                scene.name, maxDiff, edgeShifts, mismatches);
        writePng(dir.resolve(scene.name + "-legacy.png"), a, width, height);
        writePng(dir.resolve(scene.name + "-drawlist.png"), b, width, height);
        if (edgeShifts + mismatches > 0) {
            writePng(dir.resolve(scene.name + "-diff.png"), diff, width, height);
        }
    }

    private static boolean inNeighbourhood(byte[] source, int i, byte[] other, int x, int y, int width, int height) {
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int nx = x + dx, ny = y + dy;
                if (nx >= 0 && ny >= 0 && nx < width && ny < height
                        && channelDiff(source, i, other, (ny * width + nx) * 4) <= TOLERANCE) {
                    return true;
                }
            }
        }
        return false;
    }

    private static int channelDiff(byte[] a, int i, byte[] b, int j) {
        int max = 0;
        for (int c = 0; c < 3; c++) {
            max = Math.max(max, Math.abs((a[i + c] & 0xFF) - (b[j + c] & 0xFF)));
        }
        return max;
    }

    private static String rgb(byte[] data, int i) {
        return "(" + (data[i] & 0xFF) + "," + (data[i + 1] & 0xFF) + "," + (data[i + 2] & 0xFF) + ")";
    }

    private static void writePng(Path file, byte[] rgba, int width, int height) {
        try {
            Screenshot.writePng(file, rgba, width, height);
        } catch (IOException e) {
            log.error("Unable to write " + file, e);
        }
    }

    // region scenes (each stays within 256 sprites per begin/end)

    private void spritesWithTransforms(SpriteBatch batch) {
        Random random = new Random(11);
        Vector2 position = new Vector2();
        Vector2 anchor = new Vector2();
        Color tint = new Color();
        for (int i = 0; i < 200; i++) {
            position.set(random.nextFloat() * WIDTH, random.nextFloat() * HEIGHT);
            anchor.set(random.nextFloat(), random.nextFloat());
            tint.set(0.4f + random.nextFloat() * 0.6f, 0.4f + random.nextFloat() * 0.6f,
                    0.4f + random.nextFloat() * 0.6f, 0.5f + random.nextFloat() * 0.5f);
            float scale = 0.5f + random.nextFloat() * 2f;
            float rotation = random.nextInt(4) == 0 ? 0f : random.nextFloat() * 6.28f;
            batch.draw(textures[i % 6], position, null, tint, anchor, scale, scale * 0.8f, rotation, 0);
        }
    }

    private void sourceRectsAndAreas(SpriteBatch batch) {
        Texture sheet = textures[4]; // 50x85 character sheet
        for (int i = 0; i < 12; i++) {
            Rectangle source = new Rectangle((i % 3) * 16, (i / 3) * 20, 16, 20);
            batch.draw(sheet, new Vector2(40 + i * 60, 60), source, Color.WHITE, Vector2.ZERO, 3f, 3f, 0f, 0);
            batch.draw(textures[5], new Rectangle(40 + i * 60, 260, 50, 30 + i * 8), source, Color.WHITE,
                    Vector2.HALF, i * 0.2f, 0);
        }
        batch.draw(textures[3], new Rectangle(500, 400, 250, 150), Color.WHITE);
    }

    private void depthSorted(SpriteBatch batch) {
        Random random = new Random(5);
        Vector2 position = new Vector2();
        for (int i = 0; i < 250; i++) {
            position.set(100 + random.nextFloat() * 600, 100 + random.nextFloat() * 400);
            batch.draw(textures[i % 6], position, null, Color.WHITE, Vector2.HALF, 2f, 2f, 0f, random.nextInt(8));
        }
    }

    private void manyTextures(SpriteBatch batch) {
        Random random = new Random(9);
        Vector2 position = new Vector2();
        for (int i = 0; i < 240; i++) {
            position.set(random.nextFloat() * WIDTH, random.nextFloat() * HEIGHT);
            batch.draw(textures[random.nextInt(textures.length)], position, null, Color.WHITE, Vector2.HALF, 1.5f,
                    1.5f, 0f, 0);
        }
    }

    private void textAndSprites(SpriteBatch batch) {
        for (int i = 0; i < 8; i++) {
            batch.draw(textures[i % 4], new Vector2(30, 30 + i * 70), Color.WHITE);
            batch.drawText(font, "Pixel URP v2 parity line " + i + ": Hello, World!", new Vector2(80, 30 + i * 70),
                    i % 2 == 0 ? Color.WHITE : Color.ORANGE);
            batch.drawText(font, "small " + i, new Vector2(600, 40 + i * 70), Color.YELLOW, 14);
        }
    }

    // endregion

    @Override
    public void dispose() {
        legacy.dispose();
        drawList.dispose();
        for (Texture texture : textures) {
            texture.dispose();
        }
        super.dispose();
    }

    public static void main(String[] args) {
        var settings = new WindowSettings("Pixel - Render Parity", WIDTH, HEIGHT);
        settings.setWindowResizable(false);
        settings.setMultisampling(0);
        settings.setVsync(false);
        settings.setIdleThrottle(false);
        settings.setBackgroundColor(new Color(0.1f, 0.12f, 0.16f, 1f));
        DesktopLauncher.run(new RenderParityCheck(), settings);
    }
}
