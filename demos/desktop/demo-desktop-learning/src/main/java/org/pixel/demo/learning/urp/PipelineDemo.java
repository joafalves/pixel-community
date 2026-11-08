package org.pixel.demo.learning.urp;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.core.DesktopLauncher;
import org.pixel.core.ScreenSpace;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.demo.learning.common.Screenshot;
import org.pixel.graphics.canvas.Canvas;
import org.pixel.graphics.canvas.LineJoin;
import org.pixel.graphics.draw.Layer;
import org.pixel.graphics.pass.RenderPipeline;
import org.pixel.graphics.render.BlendMode;
import org.pixel.graphics.text.SdfFont;
import org.pixel.math.Vector2;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;

/**
 * URP v2 render pipeline: three layers in two passes.
 * <ul>
 *   <li>{@code world} (sorted by y): a 4000 x 3000 world of trees, rocks and crystals seen through a camera that pans
 *   and zooms by itself. Objects outside the view are culled; edges stay one pixel soft at every zoom.</li>
 *   <li>{@code glow} (additive blending): drifting particles, drawn after the world through the same camera.</li>
 *   <li>{@code hud}: screen space, unaffected by the camera.</li>
 * </ul>
 * The engine's dev tools are open: the Render tab shows each layer's commands, culling and batches; the Game tab the
 * zoom and the commands recorded.
 * Run with {@code -Dpixel.screenshot=build/pipeline.png} to save a PNG and exit ({@code -Dpixel.time=13.5} picks
 * the camera's moment; 13.5 is the most zoomed out).
 */
public class PipelineDemo extends DemoGame {

    private static final Layer WORLD = Layer.sorted("world");
    private static final Layer GLOW = Layer.ordered("glow").blend(BlendMode.ADDITIVE);
    private static final Layer HUD = Layer.ordered("hud");

    private static final float WORLD_WIDTH = 4000, WORLD_HEIGHT = 3000;
    private static final int OBJECTS = 1500, PARTICLES = 400;

    private static final Color GRASS = new Color(0.16f, 0.30f, 0.18f, 1f);
    private static final Color GRASS_DARK = new Color(0.12f, 0.24f, 0.14f, 1f);
    private static final Color TRUNK = new Color(0.40f, 0.26f, 0.15f, 1f);
    private static final Color LEAVES = new Color(0.22f, 0.55f, 0.28f, 1f);
    private static final Color LEAVES_LIGHT = new Color(0.45f, 0.75f, 0.35f, 1f);
    private static final Color ROCK = new Color(0.50f, 0.52f, 0.56f, 1f);
    private static final Color CRYSTAL = new Color(0.45f, 0.85f, 1f, 1f);
    private static final Color CRYSTAL_DEEP = new Color(0.30f, 0.30f, 0.90f, 1f);
    private static final Color SHADOW = new Color(0f, 0f, 0f, 0.35f);
    private static final Color GLOW_CORE = new Color(1f, 0.85f, 0.45f, 0.9f);
    private static final Color GLOW_EDGE = new Color(1f, 0.5f, 0.2f, 0f);
    private static final Color PANEL = new Color(0.08f, 0.09f, 0.12f, 0.85f);
    private static final Color MUTED = new Color(0.70f, 0.74f, 0.80f, 1f);

    private final String screenshot = System.getProperty("pixel.screenshot");
    private final float screenshotTime = Float.parseFloat(System.getProperty("pixel.time", "2.2"));
    private RenderPipeline pipeline;
    private SdfFont font;
    private final float[] objectX = new float[OBJECTS], objectY = new float[OBJECTS], objectSize = new float[OBJECTS];
    private final int[] objectKind = new int[OBJECTS];
    private final float[] particleX = new float[PARTICLES], particleY = new float[PARTICLES];
    private final float[] particlePhase = new float[PARTICLES];
    private final StringBuilder line = new StringBuilder();
    private int recorded;
    private float time;
    private int frames;

    @Override
    public void load() {
        super.load(); // gameCamera: the virtual resolution, centered on its position
        font = content().load("fonts/gidole-regular.ttf", org.pixel.graphics.text.SdfFont.class);
        pipeline = RenderPipeline.builder(graphics().getRenderBackend())
                .pass(gameCamera, WORLD, GLOW)
                .pass(new ScreenSpace(context()), HUD)
                .build();
        devTools().track(pipeline, "pipeline"); // its passes and layers in the dev tools' Render tab
        devTools().watch("zoom", gameCamera::getZoom).watch("commands recorded", () -> recorded, 0);

        Random random = new Random(7);
        for (int i = 0; i < OBJECTS; i++) {
            objectX[i] = random.nextFloat() * WORLD_WIDTH;
            objectY[i] = random.nextFloat() * WORLD_HEIGHT;
            objectSize[i] = 0.7f + random.nextFloat() * 0.8f;
            objectKind[i] = random.nextInt(10) < 6 ? 0 : random.nextInt(10) < 7 ? 1 : 2;
        }
        for (int i = 0; i < PARTICLES; i++) {
            particleX[i] = random.nextFloat() * WORLD_WIDTH;
            particleY[i] = random.nextFloat() * WORLD_HEIGHT;
            particlePhase[i] = random.nextFloat() * 6.283f;
        }
    }

    @Override
    public void update(DeltaTime delta) {
        super.update(delta);
        time += delta.getElapsed();
        float t = screenshot != null ? screenshotTime : time; // fixed moment for screenshots
        gameCamera.setZoom(0.95f + 0.6f * (float) Math.sin(t * 0.35f));
        gameCamera.setPosition(WORLD_WIDTH / 2 + (float) Math.cos(t * 0.13f) * 1200,
                WORLD_HEIGHT / 2 + (float) Math.sin(t * 0.17f) * 800);
    }

    @Override
    public void draw(DeltaTime delta) {
        recorded = 0;
        drawWorld(pipeline.canvas(WORLD));
        drawGlow(pipeline.canvas(GLOW));
        drawHud(pipeline.canvas(HUD));
        pipeline.render();

        if (screenshot != null && ++frames == 4) {
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

    private void drawWorld(Canvas c) {
        // ground: a checkerboard of big tiles (depth -1: below everything)
        for (int ty = 0; ty < WORLD_HEIGHT; ty += 250) {
            for (int tx = 0; tx < WORLD_WIDTH; tx += 250) {
                c.rect(tx, ty, 250, 250).fill(((tx + ty) / 250) % 2 == 0 ? GRASS : GRASS_DARK).depth(-1);
                recorded++;
            }
        }
        // objects sorted by their foot (y): whatever stands lower is drawn in front
        for (int i = 0; i < OBJECTS; i++) {
            float x = objectX[i], y = objectY[i], s = objectSize[i];
            switch (objectKind[i]) {
                case 0 -> { // tree
                    c.ellipse(x, y, 34 * s, 12 * s).fill(SHADOW).depth(y);
                    c.rect(x - 7 * s, y - 40 * s, 14 * s, 40 * s).radius(4 * s).fill(TRUNK).depth(y);
                    c.circle(x, y - 70 * s, 42 * s).fillRadial(x - 12 * s, y - 84 * s, 50 * s, LEAVES_LIGHT, LEAVES)
                            .stroke(2, GRASS_DARK).depth(y);
                    recorded += 3;
                }
                case 1 -> { // rock
                    c.roundRect(x - 26 * s, y - 30 * s, 52 * s, 32 * s, 12 * s)
                            .fillLinear(x, y - 30 * s, ROCK, x, y, GRASS_DARK).shadow(0, 4, 8, SHADOW).depth(y);
                    recorded++;
                }
                default -> { // crystal: a path
                    c.path().moveTo(x, y - 70 * s).lineTo(x + 20 * s, y - 25 * s).lineTo(x + 10 * s, y)
                            .lineTo(x - 10 * s, y).lineTo(x - 20 * s, y - 25 * s).close()
                            .fillLinear(x, y - 70 * s, CRYSTAL, x, y, CRYSTAL_DEEP).stroke(2, Color.WHITE)
                            .join(LineJoin.ROUND).depth(y);
                    recorded++;
                }
            }
        }
    }

    private void drawGlow(Canvas c) {
        float t = screenshot != null ? screenshotTime : time;
        for (int i = 0; i < PARTICLES; i++) {
            float p = particlePhase[i] + t * 0.6f;
            float x = particleX[i] + (float) Math.cos(p) * 40, y = particleY[i] + (float) Math.sin(p * 1.3f) * 30;
            c.circle(x, y, 26).fillRadial(x, y, 26, GLOW_CORE, GLOW_EDGE);
            recorded++;
        }
    }

    private void drawHud(Canvas c) {
        c.roundRect(16, 16, 360, 84, 12).fill(PANEL).stroke(1, MUTED);
        c.text(font, "Render pipeline", 32, 28).size(24).fill(Color.WHITE);
        c.text(font, "world (sorted) + glow (additive) + hud", 32, 66).size(16).fill(MUTED);
    }

    @Override
    public void dispose() {
        pipeline.dispose();
        super.dispose();
    }

    public static void main(String[] args) {
        var settings = new WindowSettings("Pixel - URP Render Pipeline", 1280, 720);
        settings.setWindowResizable(false);
        settings.setVsync(true);
        settings.setBackgroundColor(new Color(0.05f, 0.06f, 0.08f, 1f));
        DesktopLauncher.run(new PipelineDemo(), withDevTools(settings));
    }
}
