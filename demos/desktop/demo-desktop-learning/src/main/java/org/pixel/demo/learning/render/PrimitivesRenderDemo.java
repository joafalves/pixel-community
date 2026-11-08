package org.pixel.demo.learning.render;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.core.DesktopLauncher;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.graphics.canvas.Canvas;
import org.pixel.graphics.draw.Layer;
import org.pixel.graphics.pass.RenderPipeline;
import org.pixel.math.Vector2;

/**
 * Basic filled and stroked primitives with {@link Canvas}: rectangles, rounded rectangles, circles, triangles and
 * quadratic curves.
 */
public class PrimitivesRenderDemo extends DemoGame {

    private static final float STROKE = 6f;

    private static final Layer MAIN = Layer.ordered("main");

    private RenderPipeline pipeline;

    @Override
    public void load() {
        super.load();
        gameCamera.setOrigin(Vector2.zero());
        pipeline = RenderPipeline.builder(graphics().getRenderBackend()).pass(gameCamera, MAIN).build();
    }

    @Override
    public void draw(DeltaTime delta) {
        Canvas c = pipeline.canvas(MAIN);

        // rectangle and rounded rectangle
        c.rect(10, 10, 280, 180).fill(Color.RED).stroke(STROKE, Color.WHITE);
        c.roundRect(20, 20, 260, 160, 16).fill(Color.YELLOW).stroke(STROKE, Color.WHITE);

        // circle
        c.circle(450, 100, 80).fill(Color.GREEN).stroke(STROKE, Color.WHITE);

        // triangles
        c.path().moveTo(10, 210).lineTo(260, 210).lineTo(10, 390).close()
                .fill(Color.BLUE).stroke(STROKE, Color.WHITE);
        c.path().moveTo(290, 210).lineTo(290, 390).lineTo(40, 390).close()
                .fill(Color.CORAL).stroke(STROKE, Color.WHITE);

        // quadratic curves closed by a straight line
        c.path().moveTo(310, 390).quadTo(450, 200, 590, 390).close()
                .fill(Color.ROYAL).stroke(STROKE, Color.WHITE);
        c.path().moveTo(310, 210).quadTo(450, 400, 590, 210).close()
                .fill(Color.MAROON).stroke(STROKE, Color.WHITE);

        pipeline.render();
    }

    @Override
    public void dispose() {
        pipeline.dispose();
        super.dispose();
    }

    public static void main(String[] args) {
        var settings = new WindowSettings(600, 400);
        settings.setWindowResizable(false);
        settings.setVsync(true);

        DesktopLauncher.run(new PrimitivesRenderDemo(), settings);
    }
}
