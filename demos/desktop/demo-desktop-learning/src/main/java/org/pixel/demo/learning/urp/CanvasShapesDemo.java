package org.pixel.demo.learning.urp;

import org.pixel.core.DesktopLauncher;
import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.content.ContentManager;
import org.pixel.content.Texture;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.demo.learning.common.Screenshot;
import org.pixel.graphics.canvas.Canvas;
import org.pixel.graphics.draw.Layer;
import org.pixel.graphics.pass.RenderPipeline;
import org.pixel.graphics.canvas.LineCap;
import org.pixel.graphics.canvas.StrokeAlign;
import org.pixel.math.Vector2;

import java.io.IOException;
import java.nio.file.Path;

/**
 * URP v2 Canvas: SDF shapes with fill and stroke, rounded corners, lines with caps, transforms, nested clipping and
 * images, all drawn by one canvas (a handful of draw calls).
 * <p>
 * Run with {@code -Dpixel.screenshot=build/canvas.png} to save the first frames as a PNG and exit.
 */
public class CanvasShapesDemo extends DemoGame {

    private static final Color PANEL = new Color(0.13f, 0.15f, 0.20f, 1f);
    private static final Color PANEL_EDGE = new Color(0.35f, 0.40f, 0.52f, 1f);
    private static final Color ACCENT = new Color(0.96f, 0.73f, 0.26f, 1f);
    private static final Color TEAL = new Color(0.20f, 0.75f, 0.70f, 1f);
    private static final Color PINK = new Color(0.93f, 0.35f, 0.55f, 0.85f);
    private static final Color SHADOW = new Color(0f, 0f, 0f, 0.35f);

    private final String screenshot = System.getProperty("pixel.screenshot");
    private static final Layer MAIN = Layer.ordered("main");

    private RenderPipeline pipeline;
    private ContentManager content;
    private Texture earth;
    private float time;
    private int frames;

    @Override
    public void load() {
        super.load();
        gameCamera.setOrigin(Vector2.zero());
        pipeline = RenderPipeline.builder(graphics().getRenderBackend()).pass(gameCamera, MAIN).build();
        devTools().track(pipeline, "canvas"); // its passes and layers in the dev tools' Render tab
        content = content();
        earth = content.load("images/earth-48x48.png", Texture.class);
    }

    @Override
    public void update(DeltaTime delta) {
        super.update(delta);
        time += delta.getElapsed();
    }

    @Override
    public void draw(DeltaTime delta) {
        Canvas c = pipeline.canvas(MAIN);

        // fill / stroke alignment
        c.rect(40, 40, 140, 90).fill(TEAL);
        c.rect(220, 40, 140, 90).fill(TEAL).stroke(6, Color.WHITE).strokeAlign(StrokeAlign.INSIDE);
        c.rect(400, 40, 140, 90).fill(TEAL).stroke(6, Color.WHITE).strokeAlign(StrokeAlign.CENTER);
        c.rect(580, 40, 140, 90).fill(TEAL).stroke(6, Color.WHITE).strokeAlign(StrokeAlign.OUTSIDE);

        // rounded corners (uniform, per-corner, pill) and stroke-only
        c.roundRect(40, 170, 140, 90, 18).fill(ACCENT);
        c.rect(220, 170, 140, 90).radius(40, 0, 40, 0).fill(PINK).stroke(3, Color.WHITE);
        c.roundRect(400, 190, 140, 50, 25).fill(PANEL_EDGE).stroke(2, ACCENT);
        c.roundRect(580, 170, 140, 90, 12).noFill().stroke(4, ACCENT).strokeAlign(StrokeAlign.INSIDE);

        // circles, ellipses, rings
        c.circle(110, 340, 40).fill(PINK);
        c.ellipse(290, 340, 70, 30).fill(TEAL).stroke(3, Color.WHITE);
        c.circle(470, 340, 40).noFill().stroke(10, ACCENT);
        c.circle(650, 340, 3).fill(Color.WHITE); // tiny dot

        // lines: caps and widths, thin to thick
        for (int i = 0; i < 6; i++) {
            float y = 410 + i * 14;
            c.line(40, y, 300, y + 20).width(1 + i * 1.5f).cap(LineCap.values()[i % 3]).color(Color.WHITE);
        }
        c.line(360, 420, 460, 500).width(14).cap(LineCap.ROUND).color(ACCENT);
        c.line(500, 420, 600, 500).width(14).cap(LineCap.SQUARE).color(TEAL);
        c.line(640, 420, 740, 500).width(14).cap(LineCap.BUTT).color(PINK);

        // transforms: a rotating "card" with a drop shadow, scaled copies
        c.save();
        c.translate(900, 120);
        c.rotate(time * 0.6f);
        c.roundRect(-70 + 6, -45 + 8, 140, 90, 14).fill(SHADOW);
        c.roundRect(-70, -45, 140, 90, 14).fill(PANEL).stroke(3, ACCENT);
        c.circle(0, 0, 20).fill(TEAL);
        c.restore();
        for (int i = 0; i < 4; i++) {
            c.save();
            c.translate(820 + i * 45, 260);
            c.scale(0.5f + i * 0.4f);
            c.rect(-10, -10, 20, 20).radius(4).fill(ACCENT).stroke(1, Color.WHITE);
            c.restore();
        }

        // clipped, scrolling panel with nested clip and images
        float px = 800, py = 330, pw = 260, ph = 200;
        c.roundRect(px, py, pw, ph, 10).fill(PANEL).stroke(2, PANEL_EDGE).strokeAlign(StrokeAlign.OUTSIDE);
        c.clip(px + 10, py + 10, pw - 20, ph - 20, () -> {
            float scroll = (time * 40) % 120;
            for (int row = 0; row < 8; row++) {
                float y = py + 15 + row * 40 - scroll;
                c.roundRect(px + 15, y, pw - 30, 32, 6).fill(row % 2 == 0 ? PANEL_EDGE : TEAL);
                c.image(earth, px + 22, y + 4).size(24, 24);
                c.clip(px + 60, y, 120, 32, () -> c.line(px + 60, y + 16, px + 400, y + 16).width(6)
                        .cap(LineCap.ROUND).color(ACCENT)); // nested clip: the line stops at 120 px
            }
        });

        pipeline.render();

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
        pipeline.dispose();
        super.dispose();
    }

    public static void main(String[] args) {
        var settings = new WindowSettings("Pixel - URP Canvas Shapes", 1100, 560);
        settings.setWindowResizable(false);
        settings.setMultisampling(0); // anti-aliasing comes from the distance fields, not MSAA
        settings.setVsync(true);
        settings.setBackgroundColor(new Color(0.08f, 0.09f, 0.12f, 1f));
        DesktopLauncher.run(new CanvasShapesDemo(), withDevTools(settings));
    }
}
