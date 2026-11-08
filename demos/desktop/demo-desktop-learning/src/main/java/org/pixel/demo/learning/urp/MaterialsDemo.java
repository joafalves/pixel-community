package org.pixel.demo.learning.urp;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.content.Texture;
import org.pixel.core.DesktopLauncher;
import org.pixel.core.ScreenSpace;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.demo.learning.common.Screenshot;
import org.pixel.graphics.canvas.Canvas;
import org.pixel.graphics.draw.Layer;
import org.pixel.graphics.material.Material;
import org.pixel.graphics.material.SourceWatcher;
import org.pixel.graphics.pass.RenderPipeline;
import org.pixel.graphics.text.SdfFont;
import org.pixel.graphics.text.TextAlign;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.keyboard.KeyboardKey;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * URP v2 materials: custom shaders on canvas commands.
 * <ul>
 *   <li><b>Water</b> (an effect material loaded from {@code shaders/water.glsl}): animated by a {@code uTime} uniform.
 *   Run from the demo's project directory and edit {@code src/main/resources/shaders/water.glsl}: it reloads live,
 *   and a compile error keeps the last good version (the HUD shows the error).</li>
 *   <li><b>Tiles</b> in a sorted layer with {@code batchByMaterial()}: grass and water tiles share a depth, so they
 *   are grouped into one batch each. Press B to compare with a plain sorted layer.</li>
 *   <li><b>Hit flash and dissolve</b>: per-command material data ({@code .material(m, amount)}), so every planet has
 *   its own value and they still share a draw call.</li>
 *   <li><b>Swaying grass</b>: a raw material whose vertex shader moves the top of each blade.</li>
 *   <li><b>Title</b>: an effect over text (outline included).</li>
 * </ul>
 * Run with {@code -Dpixel.screenshot=build/materials.png} to save a PNG and exit.
 */
public class MaterialsDemo extends DemoGame {

    private static final Layer TILES = Layer.sorted("tiles").batchByMaterial();
    private static final Layer TILES_PLAIN = Layer.sorted("tiles-plain");
    private static final Layer WORLD = Layer.sorted("world");
    private static final Layer HUD = Layer.ordered("hud");

    private static final Path WATER_FILE = Path.of("src/main/resources/shaders/water.glsl");
    private static final int TILE = 32, COLUMNS = 40, ROWS = 23;
    private static final Color BACKGROUND = new Color(0.05f, 0.06f, 0.09f, 1f);
    private static final Color PANEL = new Color(0.06f, 0.07f, 0.12f, 0.88f);
    private static final Color MUTED = new Color(0.68f, 0.72f, 0.82f, 1f);
    private static final Color ERROR = new Color(1f, 0.45f, 0.45f, 1f);
    private static final Color[] GRASS = {new Color(0.55f, 0.8f, 0.45f, 1f), new Color(0.52f, 0.76f, 0.43f, 1f),
            new Color(0.58f, 0.83f, 0.47f, 1f)};
    private static final Color[] BLADES = {new Color(0.16f, 0.42f, 0.16f, 1f), new Color(0.22f, 0.52f, 0.2f, 1f),
            new Color(0.3f, 0.62f, 0.24f, 1f)};

    private final String screenshot = System.getProperty("pixel.screenshot");
    private final StringBuilder line = new StringBuilder();
    private RenderPipeline pipeline;
    private SdfFont font;
    private Texture grass, water, planet;
    private Material waterMaterial, flash, dissolve, sway, title;
    private SourceWatcher watcher;
    private boolean grouped = true;
    private float time;
    private int frames;

    @Override
    public void load() {
        super.load();
        gameCamera.setPosition(640, 360); // world units = window pixels at zoom 1
        font = content().load("fonts/gidole-regular.ttf", SdfFont.class);
        grass = content().loadTexture("images/green-32x32.png");
        water = content().loadTexture("images/blue-32x32.png");
        planet = content().loadTexture("images/earth-48x48.png");

        waterMaterial = Material.effect(content().load("shaders/water.glsl", String.class));
        if (Files.isRegularFile(WATER_FILE)) {
            watcher = SourceWatcher.effect(WATER_FILE, waterMaterial);
        }
        flash = Material.effect("""
                // f.data.x: how white (0 = normal, 1 = white)
                vec4 effect(vec4 color, vec2 uv, Fragment f) {
                    return vec4(mix(color.rgb, vec3(color.a), f.data.x), color.a);
                }
                """);
        dissolve = Material.effect("""
                // f.data.x: progress (0 = whole, 1 = gone); a hot rim where it burns
                float hash(vec2 p) {
                    return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453);
                }
                vec4 effect(vec4 color, vec2 uv, Fragment f) {
                    float n = hash(floor(f.local / 3.0));
                    if (n < f.data.x) discard;
                    vec4 rim = vec4(1.0, 0.55, 0.15, 1.0) * color.a;
                    return mix(rim, color, smoothstep(f.data.x, f.data.x + 0.12, n));
                }
                """);
        sway = Material.raw("""
                layout(location = 0) in vec2 aPosition;
                layout(location = 1) in vec2 aTexCoord;
                layout(location = 2) in vec4 aColor;
                layout(location = 6) in int aSlot;
                uniform mat4 uMatrix;
                uniform float uTime;
                out vec2 vUv;
                out vec4 vColor;
                flat out int vSlot;
                void main() {
                    vUv = aTexCoord;
                    vColor = aColor;
                    vSlot = aSlot;
                    float bend = (1.0 - aTexCoord.y); // 0 at the root, 1 at the tip
                    float wind = sin(uTime * 2.2 + aPosition.x * 0.025) + 0.4 * sin(uTime * 5.1 + aPosition.x * 0.11);
                    vec2 p = aPosition + vec2(wind * 7.0 * bend * bend, 0.0);
                    gl_Position = uMatrix * vec4(p, 0.0, 1.0);
                }
                """, """
                in vec2 vUv;
                in vec4 vColor;
                flat in int vSlot;
                out vec4 oColor;
                void main() {
                    vec4 c = pixelSample(vSlot, vUv) * vec4(vColor.rgb * vColor.a, vColor.a);
                    if (c.a <= 0.0) discard;
                    oColor = c;
                }
                """);
        title = Material.effect("""
                uniform float uTime;
                vec4 effect(vec4 color, vec2 uv, Fragment f) {
                    vec3 tint = 0.6 + 0.4 * cos(f.position.x * 0.012 - uTime * 1.5 + vec3(0.0, 2.1, 4.2));
                    return vec4(color.rgb * tint, color.a);
                }
                """);

        pipeline = RenderPipeline.builder(graphics().getRenderBackend())
                .pass(gameCamera, TILES, TILES_PLAIN, WORLD)
                .pass(new ScreenSpace(context()), HUD)
                .build();
        devTools().track(pipeline, "materials"); // its passes and layers in the dev tools' Render tab
    }

    @Override
    public void update(DeltaTime delta) {
        super.update(delta);
        time = screenshot != null ? 2.5f : time + delta.getElapsed();
        if (Keyboard.isKeyPressed(KeyboardKey.B)) {
            grouped = !grouped;
        }
        if (watcher != null) {
            watcher.poll();
        }
        waterMaterial.set("uTime", time);
        sway.set("uTime", time);
        title.set("uTime", time);
    }

    @Override
    public void draw(DeltaTime delta) {
        drawTiles(pipeline.canvas(grouped ? TILES : TILES_PLAIN));
        drawWorld(pipeline.canvas(WORLD));
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

    /** A grass field with a pond: every tile at depth 0, grass and water interleaved along each row. */
    private void drawTiles(Canvas c) {
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                float x = column * TILE, y = row * TILE;
                float dx = (x + 16 - 640) / 330f, dy = (y + 16 - 420) / 170f;
                boolean pond = dx * dx + dy * dy < 1f || ((column + row * 3) % 11 == 0 && dx * dx + dy * dy < 1.4f);
                if (pond) {
                    c.image(water, x, y).size(TILE, TILE).material(waterMaterial);
                } else {
                    c.image(grass, x, y).size(TILE, TILE).tint(GRASS[(column * 7 + row * 13) % GRASS.length]);
                }
            }
        }
    }

    private void drawWorld(Canvas c) {
        // grass blades around the pond, swaying (raw material: the vertex shader moves their tips)
        for (int i = 0; i < 160; i++) {
            float angle = i * 2.39996f;
            float radius = 1.08f + (i * 37 % 100) / 100f * 0.35f;
            float x = 640 + (float) Math.cos(angle) * 330 * radius, y = 420 + (float) Math.sin(angle) * 170 * radius;
            float height = 22 + (i * 53 % 17);
            c.image(grass, x, y).size(6, height).anchor(0.5f, 1f).tint(BLADES[i % BLADES.length]).depth(y)
                    .material(sway);
        }
        // planets: hit flashes and dissolves, each with its own value (material data), one batch per material
        for (int i = 0; i < 6; i++) {
            float x = 140 + i * 200, y = 120 + (float) Math.sin(time * 1.3f + i) * 16;
            float hit = Math.max(0f, 1f - fract(time * 0.6f + i / 6f) * 5f);
            c.image(planet, x, y).size(96, 96).anchor(0.5f, 0.5f).depth(y).material(flash, hit);
        }
        for (int i = 0; i < 6; i++) {
            float x = 140 + i * 200, y = 640 + (float) Math.cos(time * 1.1f + i) * 12;
            float progress = 0.5f + 0.5f * (float) Math.sin(time * 0.9f + i * 0.8f);
            c.image(planet, x, y).size(96, 96).anchor(0.5f, 0.5f).depth(y).material(dissolve, progress);
        }
        c.text(font, "MATERIALS", 640, 380).size(96).align(TextAlign.CENTER).fill(Color.WHITE)
                .stroke(5, BACKGROUND).depth(10_000).material(title);
    }

    private void drawHud(Canvas hud) {
        hud.roundRect(16, 16, 470, 150, 12).fill(PANEL).stroke(1, MUTED);
        line.setLength(0);
        line.append("B  tiles ").append(grouped ? "grouped by material" : "in a plain sorted layer")
                .append(" (draw calls: dev tools)");
        hud.text(font, line, 32, 28).size(17).fill(Color.WHITE);
        hud.text(font, "flash and dissolve: per-command data, one batch each", 32, 56).size(15).fill(MUTED);
        hud.text(font, "grass: raw material (vertex shader)", 32, 80).size(15).fill(MUTED);
        if (watcher == null) {
            hud.text(font, "run from the demo directory to edit water.glsl live", 32, 104).size(15).fill(MUTED);
        } else if (waterMaterial.getError() != null) {
            hud.text(font, "water.glsl: compile error, previous version kept", 32, 104).size(15).fill(ERROR);
            String error = waterMaterial.getError();
            int end = error.indexOf('\n');
            hud.text(font, end > 0 ? error.substring(0, end) : error, 32, 128).size(13).fill(ERROR);
        } else {
            hud.text(font, "edit src/main/resources/shaders/water.glsl: live reload", 32, 104).size(15).fill(MUTED);
        }
    }

    private static float fract(float x) {
        return x - (float) Math.floor(x);
    }

    @Override
    public void dispose() {
        pipeline.dispose();
        for (Material material : new Material[]{waterMaterial, flash, dissolve, sway, title}) {
            if (material != null) {
                material.dispose();
            }
        }
        super.dispose();
    }

    public static void main(String[] args) {
        var settings = new WindowSettings("Pixel - URP Materials", 1280, 720);
        settings.setWindowResizable(true);
        settings.setVsync(true);
        settings.setBackgroundColor(BACKGROUND);
        DesktopLauncher.run(new MaterialsDemo(), withDevTools(settings));
    }
}
