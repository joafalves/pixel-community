package org.pixel.demo.learning.urp;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.content.Texture;
import org.pixel.core.DesktopLauncher;
import org.pixel.core.ScreenSpace;
import org.pixel.core.dev.DevTools;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.demo.learning.common.Screenshot;
import org.pixel.graphics.canvas.Canvas;
import org.pixel.graphics.draw.Layer;
import org.pixel.graphics.material.Material;
import org.pixel.graphics.pass.RenderPipeline;
import org.pixel.graphics.text.SdfFont;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.keyboard.KeyboardKey;
import org.pixel.input.mouse.Mouse;
import org.pixel.input.mouse.MouseButton;
import org.pixel.math.Vector2;

import java.io.IOException;
import java.nio.file.Path;

/**
 * URP v2 frame inspection, the base of an editor: every prop is drawn with a pick id ({@code .pick(id)}), and the
 * last frame finds the top-most one under the mouse ({@code pipeline.lastFrame().pick(layer, x, y)}), with exact
 * shapes (round corners, path outlines, glyph boxes, rotated images). Hover highlights, a click selects; the outline
 * is drawn in an overlay layer, as an editor's gizmos would be. What the last frame did (per pass and layer:
 * commands, culled commands, batches, draw calls and why batches broke) is in the engine's dev tools, open at start.
 * <p>
 * Keys: arrows pan, Q/E zoom, M toggles the water material (watch the "material changed" breaks), F1 shows the
 * engine's dev tools (this pipeline is tracked in their Render tab; hover and selection are watched in their Game
 * tab). Run with {@code -Dpixel.screenshot=build/inspector.png} to save a PNG and exit;
 * {@code -Dpixel.devtools=render} (or perf, assets, log, game) opens the dev tools on that tab.
 */
public class InspectorDemo extends DemoGame {

    private static final Layer WORLD = Layer.sorted("world");
    private static final Layer OVERLAY = Layer.ordered("overlay");
    private static final Layer HUD = Layer.ordered("hud");

    private static final int PROPS = 60;
    private static final Color GROUND = new Color(0.16f, 0.2f, 0.17f, 1f);
    private static final Color PANEL = new Color(0.06f, 0.07f, 0.12f, 0.9f);
    private static final Color MUTED = new Color(0.68f, 0.72f, 0.82f, 1f);
    private static final Color HOVER = new Color(1f, 1f, 1f, 0.7f);
    private static final Color SELECTED = new Color(1f, 0.8f, 0.25f, 1f);
    private static final Color[] PALETTE = {new Color(0.93f, 0.35f, 0.55f, 1f), new Color(0.2f, 0.78f, 0.72f, 1f),
            new Color(0.97f, 0.75f, 0.28f, 1f), new Color(0.55f, 0.6f, 0.95f, 1f), new Color(0.6f, 0.85f, 0.4f, 1f)};
    private static final String[] KINDS = {"crate", "barrel", "planet", "star", "pond", "sign"};

    private final String screenshot = System.getProperty("pixel.screenshot");
    private final float[] propX = new float[PROPS], propY = new float[PROPS], propAngle = new float[PROPS];
    private final StringBuilder line = new StringBuilder();
    private final Vector2 mouse = new Vector2();
    private RenderPipeline pipeline;
    private SdfFont font;
    private Texture planet;
    private Material water;
    private boolean useMaterial = true;
    private boolean wasDown;
    private int hovered, selected;
    private float time;
    private int frames;

    @Override
    public void load() {
        super.load();
        gameCamera.setPosition(800, 500);
        font = content().load("fonts/gidole-regular.ttf", SdfFont.class);
        planet = content().loadTexture("images/earth-48x48.png");
        water = Material.effect(content().load("shaders/water.glsl", String.class)).setName("water");
        for (int i = 0; i < PROPS; i++) { // an even, fixed scatter over a 1600 x 1000 world (R2 sequence)
            propX[i] = 80 + fract(0.5f + i * 0.7548777f) * 1440;
            propY[i] = 80 + fract(0.5f + i * 0.5698403f) * 840;
            propAngle[i] = (i % 7) * 0.35f;
        }
        pipeline = RenderPipeline.builder(graphics().getRenderBackend())
                .pass(gameCamera, WORLD, OVERLAY)
                .pass(new ScreenSpace(context()), HUD)
                .build();
        if (screenshot != null) {
            selected = 13;
        }

        // the engine's dev tools (F1): this pipeline in the Render tab, the game's own values in the Game tab
        devTools().track(pipeline, "inspector");
        devTools().watch("hovered", () -> hovered, 0).watch("selected", () -> selected, 0)
                .watch("zoom", gameCamera::getZoom);
        outlines = devTools().addToggle("selection outlines", true);
        panSpeed = devTools().addSlider("pan speed", 100, 2000, 600);
        devTools().addAction("reset camera", () -> {
            gameCamera.setPosition(800, 500);
            gameCamera.setZoom(1f);
        });
        String tab = System.getProperty("pixel.devtools");
        if (tab != null) {
            devTools().setVisible(true);
            devTools().showTab(switch (tab) {
                case "perf" -> DevTools.Tab.PERFORMANCE;
                case "assets" -> DevTools.Tab.ASSETS;
                case "log" -> DevTools.Tab.LOG;
                case "game" -> DevTools.Tab.GAME;
                default -> DevTools.Tab.RENDERING;
            });
        }
    }

    private DevTools.Toggle outlines;
    private DevTools.Slider panSpeed;

    @Override
    public void update(DeltaTime delta) {
        super.update(delta);
        time += delta.getElapsed();
        float pan = panSpeed.get() * delta.getElapsed() / gameCamera.getZoom();
        if (Keyboard.isKeyDown(KeyboardKey.LEFT)) gameCamera.translate(-pan, 0);
        if (Keyboard.isKeyDown(KeyboardKey.RIGHT)) gameCamera.translate(pan, 0);
        if (Keyboard.isKeyDown(KeyboardKey.UP)) gameCamera.translate(0, -pan);
        if (Keyboard.isKeyDown(KeyboardKey.DOWN)) gameCamera.translate(0, pan);
        if (Keyboard.isKeyDown(KeyboardKey.Q)) gameCamera.setZoom(Math.max(0.3f, gameCamera.getZoom() * 0.98f));
        if (Keyboard.isKeyDown(KeyboardKey.E)) gameCamera.setZoom(Math.min(4f, gameCamera.getZoom() * 1.02f));
        if (Keyboard.isKeyPressed(KeyboardKey.M)) useMaterial = !useMaterial;
        water.set("uTime", time);

        // picking reads the last rendered frame: what the player sees
        if (screenshot != null) {
            mouse.set(propX[3], propY[3]); // hover a prop for the screenshot
        } else {
            Mouse.getVirtualPosition(mouse);
            mouse.set(gameCamera.screenToVirtualCoordinates(mouse));
        }
        // the dev tools window takes the clicks over it
        hovered = devTools().isPointerOverUi() ? 0 : pipeline.lastFrame().pick(WORLD, mouse.getX(), mouse.getY());
        boolean down = Mouse.isMouseButtonDown(MouseButton.LEFT) && !devTools().isPointerOverUi();
        if (down && !wasDown) {
            selected = hovered;
        }
        wasDown = down;
    }

    @Override
    public void draw(DeltaTime delta) {
        drawWorld(pipeline.canvas(WORLD));
        drawOverlay(pipeline.canvas(OVERLAY));
        drawHud(pipeline.canvas(HUD));
        pipeline.render();

        if (screenshot != null && ++frames == 4) {
            try {
                Screenshot.capture(context(), Path.of(screenshot));
                log.info("Screenshot written to {0}", Path.of(screenshot).toAbsolutePath());
                log.info("{0}", pipeline.lastFrame());
            } catch (IOException e) {
                log.error("Unable to write screenshot.", e);
            }
            exit();
        }
    }

    private void drawWorld(Canvas c) {
        c.rect(0, 0, 1600, 1000).fill(GROUND).depth(-1e6f);
        for (int i = 0; i < PROPS; i++) {
            drawProp(c, i, i + 1); // pick id: the prop's index + 1 (0 means "nothing")
        }
    }

    /** Draw prop {@code i}, y-sorted, every command with the prop's pick id. */
    private void drawProp(Canvas c, int i, int pickId) {
        float x = propX[i], y = propY[i];
        Color color = PALETTE[i % PALETTE.length];
        switch (i % KINDS.length) {
            case 0 -> { // a rotated crate: picked by its rotated rounded rectangle
                c.save();
                c.translate(x, y);
                c.rotate(propAngle[i]);
                c.roundRect(-34, -26, 68, 52, 10).fill(color).stroke(3, GROUND).depth(y).pick(pickId);
                c.restore();
            }
            case 1 -> c.circle(x, y, 28).fill(color).stroke(4, GROUND).depth(y).pick(pickId);
            case 2 -> c.image(planet, x, y).size(72, 72).anchor(0.5f, 0.5f).depth(y).pick(pickId);
            case 3 -> { // a star: picked inside its outline, not in the notches between its points
                Canvas.Path path = c.path();
                for (int k = 0; k < 10; k++) {
                    double a = propAngle[i] - Math.PI / 2 + k * Math.PI / 5;
                    float r = k % 2 == 0 ? 40 : 17;
                    float px = x + (float) Math.cos(a) * r, py = y + (float) Math.sin(a) * r;
                    if (k == 0) {
                        path.moveTo(px, py);
                    } else {
                        path.lineTo(px, py);
                    }
                }
                path.close().fill(color).depth(y).pick(pickId);
            }
            case 4 -> {
                var pond = c.ellipse(x, y, 70, 34).fill(new Color(0.2f, 0.45f, 0.75f, 1f)).stroke(3, GROUND)
                        .depth(y).pick(pickId);
                if (useMaterial) {
                    pond.material(water);
                }
            }
            default -> {
                sign.setLength(0);
                sign.append("SIGN ").append(i);
                c.text(font, sign, x - 36, y - 10).size(22).fill(color).stroke(2, GROUND).depth(y).pick(pickId);
            }
        }
    }

    private final StringBuilder sign = new StringBuilder();

    private void drawOverlay(Canvas c) {
        if (!outlines.isOn()) {
            return;
        }
        if (hovered != 0 && hovered != selected) {
            outline(c, hovered - 1, HOVER, 2f);
        }
        if (selected != 0) {
            outline(c, selected - 1, SELECTED, 4f);
        }
    }

    /** An outline around a prop: its bounding circle, like an editor's selection gizmo. */
    private void outline(Canvas c, int i, Color color, float width) {
        float radius = switch (i % KINDS.length) {
            case 4 -> 76;
            case 5 -> 56;
            default -> 46;
        };
        c.circle(propX[i], propY[i], radius).noFill().stroke(width, color);
    }

    private void drawHud(Canvas hud) {
        hud.roundRect(16, 16, 340, 78, 12).fill(PANEL).stroke(1, MUTED);
        line.setLength(0);
        line.append("hover: ").append(name(hovered)).append("   selected: ").append(name(selected));
        hud.text(font, line, 32, 28).size(15).fill(Color.WHITE);
        hud.text(font, useMaterial ? "M: water material on" : "M: water material off", 32, 50).size(14)
                .fill(MUTED);
        hud.text(font, "arrows pan, Q/E zoom, click selects, F1 dev tools", 32, 70).size(14).fill(MUTED);
    }

    private StringBuilder name(int pickId) {
        StringBuilder sb = nameBuffer;
        sb.setLength(0);
        if (pickId == 0) {
            return sb.append('-');
        }
        return sb.append(KINDS[(pickId - 1) % KINDS.length]).append(" #").append(pickId);
    }

    private final StringBuilder nameBuffer = new StringBuilder();

    private static float fract(float x) {
        return x - (float) Math.floor(x);
    }

    @Override
    public void dispose() {
        pipeline.dispose();
        water.dispose();
        super.dispose();
    }

    public static void main(String[] args) {
        var settings = new WindowSettings("Pixel - URP Inspector", 1280, 720);
        settings.setWindowResizable(true);
        settings.setVsync(true);
        settings.setBackgroundColor(GROUND);
        DesktopLauncher.run(new InspectorDemo(), withDevTools(settings));
    }
}
