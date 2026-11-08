package org.pixel.demo.learning.urp;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.core.Camera2D;
import org.pixel.core.DesktopLauncher;
import org.pixel.core.ScreenSpace;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.demo.learning.common.Screenshot;
import org.pixel.graphics.backend.RenderTarget;
import org.pixel.graphics.canvas.Canvas;
import org.pixel.graphics.canvas.LineJoin;
import org.pixel.graphics.draw.Layer;
import org.pixel.graphics.pass.Effects;
import org.pixel.graphics.pass.RenderPipeline;
import org.pixel.graphics.render.BlendMode;
import org.pixel.graphics.text.SdfFont;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.keyboard.KeyboardKey;
import org.pixel.math.Vector2;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * URP v2 render targets and post effects. The world is drawn into a screen-sized target, processed by a chain of
 * effects (bloom, vignette, and toggles for sepia, pixelate, film grain, tint and border) and composited on the
 * screen; the HUD is drawn afterwards, so it stays crisp. The minimap is the same world layer drawn a second time
 * into a small target through a zoomed-out camera, shown in the HUD as an image.
 * <p>
 * Keys: 1 sepia, 2 pixelate, 3 grain, 4 tint, 5 border. Run with {@code -Dpixel.screenshot=build/post.png} to save a
 * PNG and exit; {@code -Dpixel.effects=sepia,grain} turns effects on for it.
 */
public class PostEffectsDemo extends DemoGame {

    private static final Layer WORLD = Layer.sorted("world");
    private static final Layer GLOW = Layer.ordered("glow").blend(BlendMode.ADDITIVE);
    private static final Layer HUD = Layer.ordered("hud");

    private static final Color BACKGROUND = new Color(0.03f, 0.03f, 0.07f, 1f);
    private static final Color GRID = new Color(0.12f, 0.16f, 0.32f, 1f);
    private static final Color[] NEON = {
            new Color(1f, 0.25f, 0.55f, 1f), new Color(0.2f, 0.9f, 1f, 1f), new Color(1f, 0.85f, 0.2f, 1f),
            new Color(0.5f, 1f, 0.35f, 1f), new Color(0.7f, 0.4f, 1f, 1f)};
    private static final Color PANEL = new Color(0.06f, 0.07f, 0.12f, 0.88f);
    private static final Color MUTED = new Color(0.68f, 0.72f, 0.82f, 1f);
    private static final Color ON = new Color(0.45f, 1f, 0.6f, 1f);
    private static final Color GOLD_TINT = new Color(1f, 0.85f, 0.6f, 1f);
    private static final String[] NAMES = {"sepia", "pixelate", "grain", "tint", "border"};
    private static final KeyboardKey[] KEYS = {KeyboardKey.D_1, KeyboardKey.D_2, KeyboardKey.D_3,
            KeyboardKey.D_4, KeyboardKey.D_5};

    private final String screenshot = System.getProperty("pixel.screenshot");
    private final boolean[] enabled = new boolean[NAMES.length];
    private RenderPipeline pipeline;
    private RenderTarget minimap;
    private Camera2D minimapCamera;
    private SdfFont font;
    private final Effects.Sepia sepia = Effects.sepia(0f);
    private final Effects.Pixelate pixelate = Effects.pixelate(1f);
    private final Effects.Noise grain = Effects.noise(0f);
    private final Effects.Tint tint = Effects.tint(Color.WHITE);
    private final Effects.Border border = Effects.border(0f, Color.WHITE);
    private float time;
    private int frames;

    @Override
    public void load() {
        super.load();
        font = content().load("fonts/gidole-regular.ttf", SdfFont.class);
        minimapCamera = new Camera2D(1200, 900, 2400, 1800); // the whole world
        minimapCamera.setOrigin(0.5f);

        RenderPipeline.Builder builder = RenderPipeline.builder(graphics().getRenderBackend());
        RenderTarget scene = builder.screenTarget(1f);
        scene.setClearColor(BACKGROUND); // opaque: effects like the vignette then cover the whole screen
        minimap = builder.target(320, 240);
        minimap.setClearColor(new Color(0.02f, 0.02f, 0.05f, 0.9f));
        pipeline = builder
                .pass(gameCamera, scene, WORLD, GLOW)
                .pass(minimapCamera, minimap, WORLD)
                .post(scene, Effects.bloom(0.6f, 0.9f), Effects.vignette(0.4f).setRadius(0.55f), sepia, pixelate, grain, tint,
                        border)
                .pass(new ScreenSpace(context()), HUD)
                .build();
        devTools().track(pipeline, "post effects"); // its passes and layers in the dev tools' Render tab

        if (screenshot != null) {
            List<String> on = List.of(System.getProperty("pixel.effects", "").split(","));
            for (int i = 0; i < NAMES.length; i++) {
                enabled[i] = on.contains(NAMES[i]);
            }
        }
    }

    @Override
    public void update(DeltaTime delta) {
        super.update(delta);
        time += delta.getElapsed();
        for (int i = 0; i < KEYS.length; i++) {
            if (Keyboard.isKeyPressed(KEYS[i])) {
                enabled[i] = !enabled[i];
            }
        }
        sepia.setAmount(enabled[0] ? 1f : 0f);
        pixelate.setSize(enabled[1] ? 6f : 1f);
        grain.setAmount(enabled[2] ? 0.12f : 0f).setSeed(time);
        tint.setColor(enabled[3] ? GOLD_TINT : Color.WHITE);
        border.setWidth(enabled[4] ? 18f : 0f).setColor(NEON[0]);

        float t = screenshot != null ? 1.5f : time;
        gameCamera.setPosition(1200 + (float) Math.cos(t * 0.2f) * 300, 900 + (float) Math.sin(t * 0.25f) * 200);
    }

    @Override
    public void draw(DeltaTime delta) {
        float t = screenshot != null ? 1.5f : time;
        drawWorld(pipeline.canvas(WORLD), pipeline.canvas(GLOW), t);
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

    private void drawWorld(Canvas world, Canvas glow, float t) {
        for (int x = 0; x <= 2400; x += 120) {
            world.line(x, 0, x, 1800).width(2).color(GRID).depth(-1);
        }
        for (int y = 0; y <= 1800; y += 120) {
            world.line(0, y, 2400, y).width(2).color(GRID).depth(-1);
        }
        for (int i = 0; i < 60; i++) {
            float x = 120 + (i % 10) * 240 + (float) Math.sin(t + i) * 30;
            float y = 150 + (i / 10) * 280 + (float) Math.cos(t * 0.8f + i) * 30;
            Color color = NEON[i % NEON.length];
            switch (i % 3) {
                case 0 -> world.circle(x, y, 34).noFill().stroke(6, color).depth(y);
                case 1 -> world.roundRect(x - 40, y - 26, 80, 52, 14).fill(BACKGROUND).stroke(5, color).depth(y);
                default -> {
                    var path = world.path();
                    for (int k = 0; k < 10; k++) {
                        double a = t * 0.5 + i + k * Math.PI / 5;
                        float r = k % 2 == 0 ? 42 : 18;
                        if (k == 0) {
                            path.moveTo(x + (float) Math.cos(a) * r, y + (float) Math.sin(a) * r);
                        } else {
                            path.lineTo(x + (float) Math.cos(a) * r, y + (float) Math.sin(a) * r);
                        }
                    }
                    path.close().fill(color).join(LineJoin.ROUND).depth(y);
                }
            }
            glow.circle(x, y, 70).fillRadial(x, y, 70, new Color(color.getRed(), color.getGreen(),
                    color.getBlue(), 0.35f), Color.TRANSPARENT);
        }
        world.text(font, "NEON CITY", 1200, 860).size(120).align(org.pixel.graphics.text.TextAlign.CENTER)
                .fill(Color.WHITE).stroke(4, NEON[1]).depth(2000);
    }

    private void drawHud(Canvas hud) {
        hud.roundRect(16, 16, 330, 186, 12).fill(PANEL).stroke(1, MUTED);
        hud.text(font, "Post effects", 32, 28).size(24).fill(Color.WHITE);
        hud.text(font, "bloom + vignette always on", 32, 62).size(15).fill(MUTED);
        for (int i = 0; i < NAMES.length; i++) {
            hud.text(font, (i + 1) + "  " + NAMES[i], 32 + (i % 2) * 150, 92 + (i / 2) * 26).size(16)
                    .fill(enabled[i] ? ON : MUTED);
        }
        // minimap: the world layer drawn again into a 320 x 240 target
        float mx = 1280 - 16 - 240, my = 16;
        hud.image(minimap.getTexture(), mx, my).size(240, 180);
        hud.rect(mx, my, 240, 180).noFill().stroke(2, MUTED).strokeAlign(
                org.pixel.graphics.canvas.StrokeAlign.OUTSIDE);
        hud.text(font, "minimap (render target)", mx, my + 190).size(14).fill(MUTED);
    }

    @Override
    public void dispose() {
        pipeline.dispose(); // its targets and effects too
        super.dispose();
    }

    public static void main(String[] args) {
        var settings = new WindowSettings("Pixel - URP Post Effects", 1280, 720);
        settings.setWindowResizable(true);
        settings.setVsync(true);
        settings.setBackgroundColor(BACKGROUND);
        DesktopLauncher.run(new PostEffectsDemo(), withDevTools(settings));
    }
}
