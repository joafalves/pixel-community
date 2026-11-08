package org.pixel.demo.learning.benchmark;

import org.lwjgl.system.MemoryStack;
import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.content.Texture;
import org.pixel.core.Camera2D;
import org.pixel.core.DesktopLauncher;
import org.pixel.core.GameApp;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.Screenshot;
import org.pixel.graphics.backend.RenderBackend;
import org.pixel.graphics.backend.RenderTarget;
import org.pixel.graphics.canvas.Canvas;
import org.pixel.graphics.canvas.LineCap;
import org.pixel.graphics.canvas.LineJoin;
import org.pixel.graphics.canvas.StrokeAlign;
import org.pixel.graphics.draw.Layer;
import org.pixel.graphics.pass.Effects;
import org.pixel.graphics.pass.PostEffect;
import org.pixel.graphics.pass.RenderPipeline;
import org.pixel.graphics.render.BlendMode;
import org.pixel.graphics.text.SdfFont;
import org.pixel.graphics.text.TextAlign;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load;

/**
 * Golden-image render check: draws fixed scenes into 480 x 320 render targets (through cameras without a game
 * context, so the result doesn't depend on the display or its pixel ratio), reads them back and compares them with
 * the PNGs in {@code golden/}. Differences up to {@value #TOLERANCE}/255 per channel are accepted (drivers round
 * differently); a scene fails if more than {@value #MAX_BAD_RATIO} of its pixels differ by more.
 * <p>
 * Run with {@code ./gradlew :pixel-demo-desktop-learning:renderGolden}; add {@code -Dgolden.record=true} to (re)write
 * the golden images after an intended visual change. Results and diffs go to {@code build/golden/}. The JVM exits
 * with status 1 when a scene fails.
 */
public class GoldenImageCheck extends GameApp {

    private static final Logger log = LoggerFactory.getLogger(GoldenImageCheck.class);
    private static final int WIDTH = 480, HEIGHT = 320;
    private static final int TOLERANCE = 6;
    private static final double MAX_BAD_RATIO = 0.002;
    private static final Path GOLDEN = Path.of("golden");
    private static final Path OUTPUT = Path.of("build", "golden");
    private static final Layer MAIN = Layer.sorted("main");
    private static final Layer ADD = Layer.ordered("additive").blend(BlendMode.ADDITIVE);
    private static final Layer MUL = Layer.ordered("multiply").blend(BlendMode.MULTIPLY);
    private static final Layer OVERLAY = Layer.ordered("overlay");
    private static final Layer OVERLAY_ADD = Layer.ordered("overlay-additive").blend(BlendMode.ADDITIVE);

    private static final Color INK = new Color(0.08f, 0.09f, 0.13f, 1f);
    private static final Color TEAL = new Color(0.2f, 0.78f, 0.72f, 1f);
    private static final Color GOLD = new Color(0.97f, 0.75f, 0.28f, 1f);
    private static final Color PINK = new Color(0.93f, 0.35f, 0.55f, 1f);

    private final boolean record = Boolean.getBoolean("golden.record");
    private final Camera2D camera = new Camera2D(0, 0, WIDTH, HEIGHT); // one unit per pixel, origin top-left
    private SdfFont font;
    private Texture earth;
    private int failures;
    private boolean done;

    private record Scene(String name, Consumer<RenderPipeline> draw, PostEffect... effects) {
    }

    private RenderTarget overlay; // transparent target of the "transparency" scene

    @Override
    public void load() {
        font = content().load("fonts/gidole-regular.ttf", SdfFont.class);
        earth = content().loadTexture("images/earth-48x48.png");
    }

    @Override
    public void draw(DeltaTime delta) {
        if (done) {
            return;
        }
        done = true;
        RenderBackend backend = graphics().getRenderBackend();
        List<Scene> scenes = List.of(
                new Scene("shapes", p -> shapes(p.canvas(MAIN))),
                new Scene("paints", p -> paints(p.canvas(MAIN))),
                new Scene("paths", p -> paths(p.canvas(MAIN))),
                new Scene("text", p -> text(p.canvas(MAIN))),
                new Scene("images", p -> images(p.canvas(MAIN))),
                new Scene("blending", p -> blending(p.canvas(MAIN), p.canvas(ADD), p.canvas(MUL))),
                new Scene("effects", p -> paints(p.canvas(MAIN)), Effects.sepia(0.8f), Effects.vignette(0.6f),
                        Effects.pixelate(3f)),
                new Scene("bloom", p -> blending(p.canvas(MAIN), p.canvas(ADD), p.canvas(MUL)),
                        Effects.bloom(0.5f, 1f)),
                new Scene("transparency", p -> transparency(p.canvas(OVERLAY), p.canvas(OVERLAY_ADD),
                        p.canvas(MAIN))));
        List<String> failed = new ArrayList<>();
        for (Scene scene : scenes) {
            if (!check(backend, scene)) {
                failed.add(scene.name);
            }
        }
        failures = failed.size();
        if (record) {
            log.info("GOLDEN RECORDED: {0} scenes written to {1}", scenes.size(), GOLDEN.toAbsolutePath());
        } else if (failed.isEmpty()) {
            log.info("GOLDEN OK: {0} scenes match.", scenes.size());
        } else {
            log.error("GOLDEN FAILED: {0} (see {1})", failed, OUTPUT.toAbsolutePath());
        }
        exit();
    }

    private boolean check(RenderBackend backend, Scene scene) {
        RenderTarget target = backend.createTarget(WIDTH, HEIGHT);
        RenderTarget output = backend.createTarget(WIDTH, HEIGHT);
        overlay = backend.createTarget(240, 160); // transparent: premultiplied content drawn as an image
        target.setClearColor(INK);
        RenderPipeline.Builder builder = RenderPipeline.builder(backend)
                .pass(new Camera2D(0, 0, 240, 160), overlay, OVERLAY, OVERLAY_ADD)
                .pass(camera, target, MAIN, ADD, MUL);
        if (scene.effects.length > 0) {
            builder.post(target, output, scene.effects);
        }
        RenderPipeline pipeline = builder.build();
        try {
            scene.draw.accept(pipeline);
            pipeline.render();
            byte[] pixels = backend.readPixels(scene.effects.length > 0 ? output : target);
            Path actual = OUTPUT.resolve(scene.name + ".png");
            Screenshot.writePng(actual, pixels, WIDTH, HEIGHT, false);
            Path golden = GOLDEN.resolve(scene.name + ".png");
            if (record) {
                Files.createDirectories(GOLDEN);
                Files.copy(actual, golden, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                return true;
            }
            byte[] expected = readPng(golden);
            if (expected == null) {
                log.error("  {0}: no golden image at {1} (run with -Dgolden.record=true)", scene.name, golden);
                return false;
            }
            return compare(scene.name, expected, pixels);
        } catch (IOException e) {
            log.error("  {0}: {1}", scene.name, e.getMessage());
            return false;
        } finally {
            pipeline.dispose();
            target.dispose();
            output.dispose();
            overlay.dispose();
        }
    }

    private boolean compare(String name, byte[] expected, byte[] actual) throws IOException {
        int bad = 0, maxDiff = 0;
        byte[] diff = new byte[actual.length];
        for (int i = 0; i < actual.length; i += 4) {
            int d = 0;
            for (int c = 0; c < 4; c++) {
                d = Math.max(d, Math.abs((expected[i + c] & 0xFF) - (actual[i + c] & 0xFF)));
            }
            maxDiff = Math.max(maxDiff, d);
            boolean wrong = d > TOLERANCE;
            bad += wrong ? 1 : 0;
            diff[i] = (byte) (wrong ? 255 : d * 20);
            diff[i + 3] = (byte) 255;
        }
        double ratio = bad / (double) (WIDTH * HEIGHT);
        boolean ok = ratio <= MAX_BAD_RATIO;
        if (!ok) {
            Screenshot.writePng(OUTPUT.resolve(name + "-diff.png"), diff, WIDTH, HEIGHT, false);
        }
        log.info("  {0} {1}: max channel diff {2}, pixels over tolerance {3} ({4}%)", ok ? "OK  " : "FAIL", name,
                maxDiff, bad, String.format(java.util.Locale.ROOT, "%.3f", ratio * 100));
        return ok;
    }

    private static byte[] readPng(Path file) {
        if (!Files.isRegularFile(file)) {
            return null;
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1), h = stack.mallocInt(1), comp = stack.mallocInt(1);
            ByteBuffer data = stbi_load(file.toString(), w, h, comp, 4);
            if (data == null) {
                return null;
            }
            if (w.get(0) != WIDTH || h.get(0) != HEIGHT) {
                stbi_image_free(data);
                throw new IllegalStateException(file + " is " + w.get(0) + "x" + h.get(0) + ", expected "
                        + WIDTH + "x" + HEIGHT);
            }
            byte[] out = new byte[data.remaining()];
            data.get(0, out);
            stbi_image_free(data);
            return out;
        }
    }

    // region scenes

    private void shapes(Canvas c) {
        c.rect(20, 20, 120, 70).fill(TEAL);
        c.roundRect(160, 20, 120, 70, 16).fill(GOLD).stroke(4, Color.WHITE);
        c.rect(300, 20, 160, 70).radius(30, 4, 30, 4).noFill().stroke(6, PINK).strokeAlign(StrokeAlign.INSIDE);
        c.circle(80, 170, 50).fill(PINK);
        c.ellipse(220, 170, 70, 40).fill(TEAL).stroke(3, Color.WHITE);
        c.circle(380, 170, 48).noFill().stroke(5, GOLD).dash(14, 8);
        c.line(30, 270, 200, 300).width(12).color(Color.WHITE).cap(LineCap.ROUND);
        c.line(240, 300, 450, 250).width(8).color(GOLD).dash(20, 10);
        c.save();
        c.translate(240, 250);
        c.rotate(0.4f);
        c.rect(-30, -15, 60, 30).fill(GOLD);
        c.restore();
    }

    private void paints(Canvas c) {
        c.roundRect(20, 20, 200, 120, 18).fillLinear(20, 20, PINK, 220, 140, GOLD).shadow(4, 8, 12, Color.BLACK);
        c.circle(330, 80, 60).fillRadial(310, 60, 70, Color.WHITE, TEAL).shadow(0, 0, 16, PINK);
        c.roundRect(40, 190, 400, 100, 20).fillLinear(40, 0, TEAL, 440, 0, PINK).stroke(3, Color.WHITE);
    }

    private void paths(Canvas c) {
        var star = c.path();
        for (int i = 0; i < 10; i++) {
            double a = -Math.PI / 2 + i * Math.PI / 5;
            float r = i % 2 == 0 ? 80 : 32;
            float x = 110 + (float) Math.cos(a) * r, y = 150 + (float) Math.sin(a) * r;
            if (i == 0) {
                star.moveTo(x, y);
            } else {
                star.lineTo(x, y);
            }
        }
        star.close().fill(GOLD).stroke(4, PINK).join(LineJoin.ROUND);
        c.path().moveTo(240, 260).cubicTo(260, 40, 400, 40, 440, 260).stroke(10, TEAL).cap(LineCap.ROUND);
        c.path().moveTo(230, 120).lineTo(300, 60).lineTo(370, 120).lineTo(440, 60).stroke(12, Color.WHITE)
                .join(LineJoin.MITER);
    }

    private void text(Canvas c) {
        c.text(font, "Golden", 240, 20).size(84).align(TextAlign.CENTER).fillLinear(0, 30, GOLD, 0, 110, PINK)
                .stroke(3, Color.WHITE).shadow(3, 4, 4, Color.BLACK);
        c.text(font, "Distance-field text: wrapping, kerning (AV, To), accents (ação).", 20, 150).size(20)
                .maxWidth(440).fill(Color.WHITE);
        c.text(font, "12 px small text", 20, 250).size(12).fill(TEAL);
        c.text(font, "glow", 300, 230).size(48).fill(Color.WHITE).shadow(0, 0, 8, TEAL);
    }

    private void images(Canvas c) {
        c.image(earth, 20, 20).size(96, 96);
        c.image(earth, 140, 20).size(192, 192).tint(PINK);
        c.save();
        c.translate(400, 100);
        c.rotate(0.6f);
        c.image(earth, 0, 0).size(96, 96).anchor(0.5f, 0.5f);
        c.restore();
        c.clip(20, 230, 200, 60);
        c.image(earth, 20, 200).size(120, 120);
    }

    private void blending(Canvas main, Canvas add, Canvas mul) {
        main.rect(20, 20, 440, 280).fill(new Color(0.3f, 0.32f, 0.4f, 1f));
        for (int i = 0; i < 5; i++) {
            add.circle(80 + i * 80, 110, 50).fillRadial(80 + i * 80, 110, 50, GOLD, Color.TRANSPARENT);
        }
        mul.rect(40, 200, 400, 80).fillLinear(40, 0, PINK, 440, 0, TEAL);
    }

    /**
     * Semi-transparent shapes and additive light in a transparent target, drawn twice as an image over a
     * checkerboard: premultiplied target content must blend like straight-alpha images, light included.
     */
    private void transparency(Canvas overlay, Canvas light, Canvas main) {
        for (int y = 0; y < HEIGHT; y += 40) {
            for (int x = 0; x < WIDTH; x += 40) {
                main.rect(x, y, 40, 40).fill(((x + y) / 40) % 2 == 0 ? Color.WHITE : TEAL).depth(-1);
            }
        }
        overlay.roundRect(20, 20, 200, 120, 24).fill(new Color(0.93f, 0.35f, 0.55f, 0.5f)).stroke(4, GOLD);
        overlay.circle(70, 80, 30).fill(new Color(0f, 0f, 0f, 0.6f));
        light.circle(160, 80, 50).fillRadial(160, 80, 50, GOLD, Color.TRANSPARENT);
        main.image(this.overlay.getTexture(), 10, 10);
        main.save();
        main.translate(240, 150);
        main.rotate(0.25f);
        main.image(this.overlay.getTexture(), 0, 0).size(220, 147).tint(new Color(1f, 1f, 1f, 0.7f));
        main.restore();
    }

    // endregion

    @Override
    public void dispose() {
        // fonts and textures belong to the content manager
    }

    public static void main(String[] args) {
        var settings = new WindowSettings("Pixel - Golden images", 480, 320);
        settings.setWindowResizable(false);
        settings.setVsync(false);
        GoldenImageCheck app = new GoldenImageCheck();
        DesktopLauncher.run(app, settings);
        System.exit(app.failures > 0 ? 1 : 0);
    }
}
