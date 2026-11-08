package org.pixel.demo.learning.urp;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.content.Texture;
import org.pixel.core.DesktopLauncher;
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
 * URP v2 Canvas paints and effects: linear/radial gradients, soft drop shadows (Gaussian, closed form for rounded
 * rectangles), dashed lines and circles ("marching ants"), and nine-patch panels.
 * <p>
 * Run with {@code -Dpixel.screenshot=build/canvas-paint.png} to save the first frames as a PNG and exit.
 */
public class CanvasPaintDemo extends DemoGame {

    private static final Color BG_CARD = new Color(0.16f, 0.18f, 0.24f, 1f);
    private static final Color SHADOW = new Color(0f, 0f, 0f, 0.55f);
    private static final Color GOLD = new Color(0.96f, 0.73f, 0.26f, 1f);
    private static final Color ORANGE = new Color(0.95f, 0.42f, 0.21f, 1f);
    private static final Color TEAL = new Color(0.20f, 0.75f, 0.70f, 1f);
    private static final Color BLUE = new Color(0.25f, 0.45f, 0.95f, 1f);
    private static final Color PURPLE = new Color(0.55f, 0.30f, 0.85f, 1f);
    private static final Color GLOW = new Color(0.35f, 0.85f, 1f, 0.9f);
    private static final Color TRANSPARENT_TEAL = new Color(0.20f, 0.75f, 0.70f, 0f);

    private final String screenshot = System.getProperty("pixel.screenshot");
    private static final Layer MAIN = Layer.ordered("main");

    private RenderPipeline pipeline;
    private Texture frame;
    private float time;
    private int frames;

    @Override
    public void load() {
        super.load();
        gameCamera.setOrigin(Vector2.zero());
        pipeline = RenderPipeline.builder(graphics().getRenderBackend()).pass(gameCamera, MAIN).build();
        frame = content().loadTexture("images/frame-ninepatch-24x24.png");
    }

    @Override
    public void update(DeltaTime delta) {
        super.update(delta);
        time += delta.getElapsed();
    }

    @Override
    public void draw(DeltaTime delta) {
        Canvas c = pipeline.canvas(MAIN);

        // cards with soft shadows (blur and offset grow left to right)
        for (int i = 0; i < 4; i++) {
            float x = 40 + i * 190;
            c.roundRect(x, 40, 160, 100, 14).fill(BG_CARD)
                    .shadow(0, 2 + i * 4, 4 + i * 10, SHADOW);
        }
        // shadow without blur, and a colored glow (shadow with no offset and a spread)
        c.roundRect(800, 40, 120, 100, 14).fill(GOLD).shadow(10, 10, 0, SHADOW);
        c.circle(1010, 90, 40).fill(TEAL).shadow(0, 0, 30, 6, GLOW);

        // gradients: horizontal, vertical, diagonal on a pill, radial "sphere", gradient with fading alpha
        c.roundRect(40, 190, 220, 70, 10).fillLinear(40, 0, ORANGE, 260, 0, PURPLE);
        c.roundRect(290, 190, 220, 70, 10).fillLinear(0, 190, BLUE, 0, 260, TEAL).stroke(2, Color.WHITE);
        c.roundRect(540, 200, 220, 50, 25).fillLinear(540, 200, GOLD, 760, 250, ORANGE)
                .shadow(0, 6, 12, SHADOW);
        c.circle(830, 225, 45).fillRadial(815, 205, 70, Color.WHITE, BLUE).shadow(0, 8, 16, SHADOW);
        c.ellipse(990, 225, 70, 40).fillRadial(990, 225, 70, TEAL, TRANSPARENT_TEAL);

        // dashed lines (animated phase), dashed circles (marching ants)
        for (int i = 0; i < 4; i++) {
            float y = 310 + i * 22;
            c.line(40, y, 460, y).width(2 + i * 2).dash(10 + i * 6, 8).dashPhase(-time * 40).color(Color.WHITE);
        }
        c.line(40, 410, 460, 410).width(6).dash(2, 10).cap(LineCap.BUTT).color(GOLD);
        c.circle(560, 360, 50).noFill().stroke(3, Color.WHITE).dash(10, 6).dashPhase(time * 30);
        c.circle(690, 360, 50).fill(new Color(1f, 1f, 1f, 0.08f)).stroke(6, GOLD).strokeAlign(StrokeAlign.INSIDE)
                .dash(16, 8).dashPhase(-time * 20);
        c.ellipse(840, 360, 80, 40).noFill().stroke(2, TEAL).dash(6, 6);

        // nine-patch panels: same 24x24 texture, any size; corners stay crisp
        c.ninePatch(frame, 40, 450, 300, 100).insets(8);
        c.ninePatch(frame, 370, 450, 120, 160).insets(8);
        c.ninePatch(frame, 520, 450, 40, 40).insets(8);
        c.ninePatch(frame, 590, 460, 470, 60).insets(8).tint(TEAL);
        c.image(frame, 590, 540).size(96, 96); // the same texture just stretched, for comparison

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
        var settings = new WindowSettings("Pixel - URP Canvas Paints & Effects", 1100, 640);
        settings.setWindowResizable(false);
        settings.setVsync(true);
        settings.setBackgroundColor(new Color(0.08f, 0.09f, 0.12f, 1f));
        DesktopLauncher.run(new CanvasPaintDemo(), settings);
    }
}
