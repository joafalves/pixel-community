package org.pixel.demo.learning.urp;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.core.DesktopLauncher;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.demo.learning.common.Screenshot;
import org.pixel.graphics.canvas.Canvas;
import org.pixel.graphics.canvas.CanvasBatch;
import org.pixel.graphics.canvas.LineCap;
import org.pixel.graphics.canvas.LineJoin;
import org.pixel.math.Vector2;

import java.io.IOException;
import java.nio.file.Path;

/**
 * URP v2 Canvas paths: concave polygons, quadratic/cubic curves and arcs, filled (anti-aliased fringe) and stroked
 * (miter/bevel/round joins, butt/square/round caps), including gradient fills and transforms.
 * <p>
 * Run with {@code -Dpixel.screenshot=build/canvas-path.png} to save the first frames as a PNG and exit.
 */
public class CanvasPathDemo extends DemoGame {

    private static final Color GOLD = new Color(0.96f, 0.73f, 0.26f, 1f);
    private static final Color ORANGE = new Color(0.95f, 0.42f, 0.21f, 1f);
    private static final Color TEAL = new Color(0.20f, 0.75f, 0.70f, 1f);
    private static final Color PINK = new Color(0.93f, 0.35f, 0.55f, 1f);
    private static final Color DARK_RED = new Color(0.55f, 0.08f, 0.20f, 1f);
    private static final Color PANEL = new Color(0.16f, 0.18f, 0.24f, 1f);

    private final String screenshot = System.getProperty("pixel.screenshot");
    private CanvasBatch canvasBatch;
    private float time;
    private int frames;

    @Override
    public void load() {
        super.load();
        gameCamera.setOrigin(Vector2.zero());
        canvasBatch = new CanvasBatch(graphics().getRenderBackend());
    }

    @Override
    public void update(DeltaTime delta) {
        super.update(delta);
        time += delta.getElapsed();
    }

    @Override
    public void draw(DeltaTime delta) {
        Canvas c = canvasBatch.begin(gameCamera.getViewMatrix());

        // star: concave polygon, filled with a stroke
        star(c, 120, 130, 90, 38, 5).fill(GOLD).stroke(4, ORANGE).join(LineJoin.ROUND);

        // heart: cubic curves with a radial gradient
        c.path().moveTo(330, 95)
                .cubicTo(330, 60, 270, 50, 270, 105)
                .cubicTo(270, 150, 330, 180, 330, 205)
                .cubicTo(330, 180, 390, 150, 390, 105)
                .cubicTo(390, 50, 330, 60, 330, 95)
                .close()
                .fillRadial(310, 100, 120, PINK, DARK_RED).stroke(3, Color.WHITE);

        // pie chart from arcs
        float[] slices = {0.35f, 0.25f, 0.2f, 0.2f};
        Color[] colors = {TEAL, GOLD, PINK, ORANGE};
        float angle = -(float) Math.PI / 2;
        for (int i = 0; i < slices.length; i++) {
            float sweep = slices[i] * (float) (Math.PI * 2);
            c.path().moveTo(560, 130).arc(560, 130, 85, angle, angle + sweep).close()
                    .fill(colors[i]).stroke(2, PANEL).join(LineJoin.BEVEL);
            angle += sweep;
        }

        // rotating star under the canvas transform
        c.save();
        c.translate(800, 130);
        c.rotate(time * 0.8f);
        c.scale(1f + 0.15f * (float) Math.sin(time * 2));
        star(c, 0, 0, 80, 30, 7).fillLinear(-80, 0, TEAL, 80, 0, PINK);
        c.restore();

        // joins: miter / bevel / round on the same zig-zag
        LineJoin[] joins = {LineJoin.MITER, LineJoin.BEVEL, LineJoin.ROUND};
        for (int i = 0; i < joins.length; i++) {
            float x = 40 + i * 230;
            c.path().moveTo(x, 330).lineTo(x + 50, 270).lineTo(x + 100, 330).lineTo(x + 150, 270).lineTo(x + 190, 320)
                    .stroke(18, i == 0 ? GOLD : i == 1 ? TEAL : PINK).join(joins[i]);
        }

        // caps: butt / square / round (thin guide shows the real end points)
        LineCap[] caps = {LineCap.BUTT, LineCap.SQUARE, LineCap.ROUND};
        for (int i = 0; i < caps.length; i++) {
            float y = 400 + i * 45;
            c.path().moveTo(80, y).lineTo(380, y).stroke(22, Color.WHITE).cap(caps[i]);
            c.line(80, y - 20, 80, y + 20).width(1).color(ORANGE);
            c.line(380, y - 20, 380, y + 20).width(1).color(ORANGE);
        }

        // smooth wave (quadratic curves) and a thin cubic spiral-ish curve
        var wave = c.path().moveTo(460, 420);
        for (int i = 0; i < 6; i++) {
            float x = 460 + i * 60;
            wave.quadTo(x + 30, i % 2 == 0 ? 360 : 480, x + 60, 420);
        }
        wave.stroke(5, TEAL).cap(LineCap.ROUND).join(LineJoin.ROUND);
        c.path().moveTo(460, 520).cubicTo(560, 420, 720, 620, 820, 500).stroke(1.5f, GOLD);

        // filled closed curve with gradient + translucent overlap
        c.path().moveTo(900, 380).quadTo(1060, 380, 1060, 460).quadTo(1060, 560, 940, 540).quadTo(860, 520, 900, 380)
                .close().fillLinear(900, 380, new Color(0.25f, 0.45f, 0.95f, 1f), 1060, 560, TEAL)
                .stroke(2, Color.WHITE);

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

    private static Canvas.Path star(Canvas c, float cx, float cy, float outer, float inner, int points) {
        Canvas.Path path = c.path();
        for (int i = 0; i < points * 2; i++) {
            double a = -Math.PI / 2 + i * Math.PI / points;
            float r = i % 2 == 0 ? outer : inner;
            float x = cx + (float) Math.cos(a) * r, y = cy + (float) Math.sin(a) * r;
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        return path.close();
    }

    @Override
    public void dispose() {
        canvasBatch.dispose();
        super.dispose();
    }

    public static void main(String[] args) {
        var settings = new WindowSettings("Pixel - URP Canvas Paths", 1100, 600);
        settings.setWindowResizable(false);
        settings.setVsync(true);
        settings.setBackgroundColor(new Color(0.08f, 0.09f, 0.12f, 1f));
        DesktopLauncher.run(new CanvasPathDemo(), settings);
    }
}
