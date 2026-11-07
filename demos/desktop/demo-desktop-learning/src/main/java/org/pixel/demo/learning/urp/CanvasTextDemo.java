package org.pixel.demo.learning.urp;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.core.DesktopLauncher;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.demo.learning.common.Screenshot;
import org.pixel.graphics.canvas.Canvas;
import org.pixel.graphics.canvas.CanvasBatch;
import org.pixel.graphics.canvas.StrokeAlign;
import org.pixel.graphics.text.SdfFont;
import org.pixel.graphics.text.SdfFontSettings;
import org.pixel.graphics.text.TextAlign;
import org.pixel.graphics.text.TextBaseline;
import org.pixel.math.Vector2;

import java.io.IOException;
import java.nio.file.Path;

/**
 * URP v2 distance-field text: one MTSDF atlas generated at load time (msdfgen) drawn from 8 px to 220 px, rotated
 * and zoomed, with outlines, glows, drop shadows, weight, gradients, wrapping, alignment and ellipsis.
 * <p>
 * Run with {@code -Dpixel.screenshot=build/canvas-text.png} to save the first frames as a PNG and exit.
 */
public class CanvasTextDemo extends DemoGame {

    private static final Color GOLD = new Color(0.98f, 0.78f, 0.30f, 1f);
    private static final Color ORANGE = new Color(0.95f, 0.42f, 0.21f, 1f);
    private static final Color TEAL = new Color(0.20f, 0.80f, 0.75f, 1f);
    private static final Color PINK = new Color(0.93f, 0.35f, 0.55f, 1f);
    private static final Color INK = new Color(0.07f, 0.08f, 0.11f, 1f);
    private static final Color MUTED = new Color(0.62f, 0.66f, 0.74f, 1f);
    private static final Color PANEL = new Color(0.14f, 0.16f, 0.21f, 1f);
    private static final Color SHADOW = new Color(0f, 0f, 0f, 0.6f);
    private static final Color GLOW = new Color(0.30f, 0.85f, 1f, 0.9f);

    private static final float[] SIZES = {8, 10, 12, 14, 16, 20, 24, 32, 48, 64};
    private static final String PARAGRAPH = "Distance-field text stays sharp at any size. This paragraph wraps "
            + "inside a fixed width, is centered, and keeps kerning (AV, To, Wa) intact. Olá, ação, über, café!";

    private final String screenshot = System.getProperty("pixel.screenshot");
    private CanvasBatch canvasBatch;
    private SdfFont font;
    private SdfFont wideFont;
    private float time;
    private int frames;

    @Override
    public void load() {
        super.load();
        gameCamera.setOrigin(Vector2.zero());
        canvasBatch = new CanvasBatch(graphics().getRenderBackend());
        long start = System.nanoTime();
        font = content().load("fonts/gidole-regular.ttf", SdfFont.class);
        log.info("MTSDF font ready in {0} ms ({1} glyphs, {2}x{3} atlas).", (System.nanoTime() - start) / 1_000_000,
                font.glyphCount(), font.getAtlas().getWidth(), font.getAtlas().getHeight());
        // same font with a 3x wider distance range: room for wide outlines and glows (cached on disk after the
        // first run; the cached .json/.png pair can also be shipped as a pre-baked atlas)
        wideFont = content().load("fonts/gidole-regular.ttf", SdfFont.class, SdfFontSettings.builder()
                .distanceRange(24).cacheDirectory("build/font-cache").build());
    }

    @Override
    public void update(DeltaTime delta) {
        super.update(delta);
        time += delta.getElapsed();
    }

    @Override
    public void draw(DeltaTime delta) {
        Canvas c = canvasBatch.begin(gameCamera.getViewMatrix());

        // size ramp: 8 to 64 px
        float y = 24;
        for (float size : SIZES) {
            c.text(font, (int) size + " px  Quick brown fox", 24, y).size(size).fill(Color.WHITE);
            y += size * 1.25f + 4;
        }

        // big title: gradient fill, outline and soft drop shadow
        c.text(font, "Pixel", 1250, 20).align(TextAlign.RIGHT).size(220)
                .fillLinear(0, 60, GOLD, 0, 230, ORANGE)
                .stroke(5, INK)
                .shadow(6, 8, 8, SHADOW);

        // effect samples
        float ex = 740, ey = 270;
        c.text(font, "Outline", ex, ey).size(48).fill(Color.WHITE).stroke(4, PINK);
        c.text(wideFont, "Glow", ex + 210, ey).size(48).fill(Color.WHITE).shadow(0, 0, 10, GLOW);
        c.text(font, "Shadow", ex + 350, ey).size(48).fill(TEAL).shadow(3, 4, 4, SHADOW);
        c.text(font, "Thin", ex, ey + 60).size(48).fill(Color.WHITE).weight(-0.6f);
        c.text(font, "Regular", ex + 100, ey + 60).size(48).fill(Color.WHITE);
        c.text(font, "Bold", ex + 270, ey + 60).size(48).fill(Color.WHITE).weight(1.6f);
        c.text(font, "Inner", ex + 400, ey + 60).size(48).fill(Color.WHITE).stroke(1.5f, PINK)
                .strokeAlign(StrokeAlign.INSIDE);
        // outline beyond the range (8 px range at a 48 px atlas size = 3.5 px reach at 48 px): clamped, no artifacts;
        // the wide-range font fits a 10 px outline
        c.text(font, "Limit", ex, ey + 120).size(48).fill(Color.WHITE).stroke(30, ORANGE);
        c.text(wideFont, "Wide", ex + 150, ey + 120).size(48).fill(Color.WHITE).stroke(10, ORANGE);
        c.text(font, "Radial", ex + 310, ey + 120).size(48)
                .fillRadial(ex + 380, ey + 145, 70, Color.WHITE, PINK);

        // paragraph: wrapped, centered, in a panel
        float px = 752, py = 470, pw = 270;
        c.rect(px - 12, py - 12, pw + 24, 150).radius(10).fill(PANEL);
        c.text(font, PARAGRAPH, px + pw / 2, py).size(18).maxWidth(pw).align(TextAlign.CENTER).fill(MUTED);

        // ellipsis and max lines
        c.rect(1058, py - 12, 210, 150).radius(10).fill(PANEL);
        c.text(font, "A long label that does not fit in its box", 1070, py).size(18).maxWidth(186).wrap(false)
                .ellipsis(true).fill(Color.WHITE);
        c.text(font, PARAGRAPH, 1070, py + 34).size(16).maxWidth(186).maxLines(4).ellipsis(true).fill(MUTED);

        // rotation and zoom
        c.save();
        c.translate(420, 610);
        c.rotate(time * 0.6f);
        c.text(font, "rotating", 0, 0).size(28).align(TextAlign.CENTER).baseline(TextBaseline.MIDDLE)
                .fill(Color.WHITE).stroke(2, PINK);
        c.restore();
        c.save();
        c.translate(140, 610);
        c.scale(0.5f + 3.5f * (0.5f + 0.5f * (float) Math.sin(time * 0.8f)));
        c.text(font, "zoom", 0, 0).size(12).align(TextAlign.CENTER).baseline(TextBaseline.MIDDLE)
                .fill(GOLD).stroke(1, INK);
        c.restore();

        canvasBatch.end();

        if (screenshot != null && ++frames == 3) {
            try {
                Screenshot.capture(context(), Path.of(screenshot));
                log.info("Screenshot written to {0}", Path.of(screenshot).toAbsolutePath());
                log.info("Previous frame: {0}", graphics().getStats().lastFrame());
            } catch (IOException e) {
                log.error("Unable to write screenshot.", e);
            }
            exit();
        }
    }

    @Override
    public void dispose() {
        canvasBatch.dispose();
        super.dispose();
    }

    public static void main(String[] args) {
        var settings = new WindowSettings("Pixel - URP Canvas Text", 1280, 720);
        settings.setWindowResizable(false);
        settings.setVsync(true);
        settings.setBackgroundColor(new Color(0.08f, 0.09f, 0.12f, 1f));
        DesktopLauncher.run(new CanvasTextDemo(), settings);
    }
}
