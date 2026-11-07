package org.pixel.demo.learning.benchmark;

import org.pixel.core.DesktopLauncher;
import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.content.ContentManager;
import org.pixel.content.Font;
import org.pixel.content.Texture;
import org.pixel.content.importer.settings.FontImporterSettings;
import org.pixel.core.Camera2D;
import org.pixel.core.GameApp;
import org.pixel.core.WindowSettings;
import org.pixel.graphics.RenderStats;
import org.pixel.graphics.render.DrawListSpriteBatch;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.graphics.render.opengl.GLSpriteBatch;
import org.pixel.math.Vector2;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Random;

import static org.lwjgl.opengl.GL11C.GL_RENDERER;
import static org.lwjgl.opengl.GL11C.glGetString;

/**
 * Render benchmark for URP v2 (see docs/design/urp-v2.md and urp-v2-baseline.md).
 * <p>
 * Runs every scenario on every renderer in the same session (warm-up + measurement each), prints a side-by-side
 * table and writes a CSV to {@code build/benchmarks/}. Scenarios only use the public {@link SpriteBatch} API, so the
 * classic {@link GLSpriteBatch} and the draw-list {@link DrawListSpriteBatch} run identical workloads.
 * <p>
 * Run with: {@code ./gradlew :pixel-demo-desktop-learning:renderBenchmark}
 * <br>Options (system properties): {@code -Dbench.scale=1.0} (object counts), {@code -Dbench.warmup=2}
 * and {@code -Dbench.measure=8} (seconds), {@code -Dbench.only=name} (single scenario),
 * {@code -Dbench.renderers=legacy,drawlist} (which renderers to run).
 */
public class RenderBenchmark extends GameApp {

    private static final Logger log = LoggerFactory.getLogger(RenderBenchmark.class);
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;

    private final float scale = Float.parseFloat(System.getProperty("bench.scale", "1.0"));
    private final float warmupSeconds = Float.parseFloat(System.getProperty("bench.warmup", "2"));
    private final float measureSeconds = Float.parseFloat(System.getProperty("bench.measure", "8"));
    private final String only = System.getProperty("bench.only");
    private final List<String> rendererNames =
            List.of(System.getProperty("bench.renderers", "legacy,drawlist").split(","));

    private ContentManager content;
    private final Map<String, SpriteBatch> renderers = new LinkedHashMap<>();
    private SpriteBatch spriteBatch; // renderer of the current run
    private Camera2D camera;
    private GpuTimer gpuTimer;
    private Texture[] textures;
    private Font font;

    private final List<Scenario> scenarios = new ArrayList<>();
    private final List<Run> runs = new ArrayList<>();
    private final List<Result> results = new ArrayList<>();
    private int runIndex = -1;
    private boolean measuring;
    private double phaseStart;
    private long lastFrameStart;
    private final Samples frameMs = new Samples();
    private final Samples cpuMs = new Samples();
    private final Samples gpuMs = new Samples();
    private long drawCalls, primitives, vertices, textureBinds, uploadedBytes, statFrames;
    private boolean previousFrameMeasured;

    @Override
    public void load() {
        content = content();
        for (String name : rendererNames) {
            SpriteBatch renderer = switch (name.trim()) {
                case "legacy" -> new GLSpriteBatch(graphics().getStats());
                case "drawlist" -> new DrawListSpriteBatch(graphics().getRenderBackend());
                default -> throw new IllegalArgumentException("Unknown renderer '" + name + "' (legacy, drawlist).");
            };
            renderer.init();
            renderers.put(name.trim(), renderer);
        }
        camera = new Camera2D(context());
        camera.setOrigin(0, 0);
        gpuTimer = new GpuTimer();

        // 32 distinct GL textures (re-loading the same files without cache creates separate textures)
        String[] files = {"images/blue-32x32.png", "images/green-32x32.png", "images/red-32x32.png",
                "images/earth-48x48.png"};
        textures = new Texture[32];
        for (int i = 0; i < textures.length; i++) {
            textures[i] = content.load(files[i % files.length], Texture.class, null, false);
        }
        font = content.load("fonts/gidole-regular.ttf", Font.class, new FontImporterSettings(16, 1));

        int n = Math.max(1, (int) (20_000 * scale));
        scenarios.add(new SpriteScenario("sprites-1-texture", n, 1, 1, false));
        scenarios.add(new SpriteScenario("sprites-32-textures", n, 32, 1, false));
        scenarios.add(new SpriteScenario("sprites-depth-sorted", n, 4, 10, false));
        scenarios.add(new SpriteScenario("sprites-rotated", n, 4, 1, true));
        scenarios.add(new TextScenario("text", Math.max(1, (int) (400 * scale))));
        scenarios.add(new InterleavedScenario("interleaved-sprite-text", Math.max(1, (int) (2_000 * scale))));
        if (only != null) {
            List<String> names = scenarios.stream().map(s -> s.name).toList();
            scenarios.removeIf(s -> !s.name.equals(only));
            if (scenarios.isEmpty()) {
                log.error("Unknown scenario '{0}'. Available: {1}", only, names);
            }
        }

        for (Scenario scenario : scenarios) {
            for (Map.Entry<String, SpriteBatch> renderer : renderers.entrySet()) {
                runs.add(new Run(scenario, renderer.getKey(), renderer.getValue()));
            }
        }
        log.info("Renderers: {0} ({1})", renderers.keySet(), glGetString(GL_RENDERER));
        nextRun(0);
    }

    @Override
    public void update(DeltaTime delta) {
        if (runIndex >= runs.size()) {
            return;
        }
        // The device publishes a frame's counters once it ends, so the previous frame is complete now.
        if (previousFrameMeasured) {
            accumulateFrameStats(graphics().getStats().lastFrame());
        }

        double now = delta.getTotalElapsed();
        double elapsed = now - phaseStart;
        if (!measuring && elapsed >= warmupSeconds) {
            measuring = true;
            phaseStart = now;
            resetMeasurements();
        } else if (measuring && elapsed >= measureSeconds) {
            results.add(collect(runs.get(runIndex)));
            nextRun(now);
            return;
        }
        runs.get(runIndex).scenario.update(delta.getElapsed());
    }

    @Override
    public void draw(DeltaTime delta) {
        if (runIndex >= runs.size()) {
            return;
        }
        long frameStart = System.nanoTime();
        if (measuring && lastFrameStart != 0) {
            frameMs.add((frameStart - lastFrameStart) / 1e6);
        }
        lastFrameStart = frameStart;

        gpuTimer.begin();
        runs.get(runIndex).scenario.draw(spriteBatch);
        gpuTimer.end();
        long cpuEnd = System.nanoTime();

        if (measuring) {
            cpuMs.add((cpuEnd - frameStart) / 1e6);
            double gpu = gpuTimer.lastResultMs();
            if (gpu >= 0) {
                gpuMs.add(gpu);
            }
        }
        previousFrameMeasured = measuring;
    }

    private void accumulateFrameStats(RenderStats.Frame frame) {
        drawCalls += frame.getDrawCalls();
        primitives += frame.getPrimitives();
        vertices += frame.getVertices();
        textureBinds += frame.getTextureBinds();
        uploadedBytes += frame.getUploadedBytes();
        statFrames++;
    }

    @Override
    public void dispose() {
        if (gpuTimer != null) {
            gpuTimer.dispose();
        }
        renderers.values().forEach(SpriteBatch::dispose);
        if (content != null) {
            for (Texture texture : textures) {
                texture.dispose(); // not cached (loaded with useCache=false), so not released by the content manager
            }
        }
        super.dispose();
    }

    // region scenario control

    private void nextRun(double now) {
        runIndex++;
        measuring = false;
        phaseStart = now;
        lastFrameStart = 0;
        if (runIndex < runs.size()) {
            Run run = runs.get(runIndex);
            run.scenario.setup(new Random(42)); // same seed: every renderer gets the identical workload
            spriteBatch = run.renderer;
            log.info("Run {0}/{1}: {2} on {3}", runIndex + 1, runs.size(), run.scenario.name, run.rendererName);
        } else {
            report();
            exit();
        }
    }

    private void resetMeasurements() {
        frameMs.clear();
        cpuMs.clear();
        gpuMs.clear();
        drawCalls = primitives = vertices = textureBinds = uploadedBytes = statFrames = 0;
        lastFrameStart = 0;
        gpuTimer.discardPending(); // in-flight queries may still cover warm-up or previous-scenario frames
    }

    private Result collect(Run run) {
        double f = Math.max(1, statFrames);
        return new Result(run.scenario.name, run.rendererName, run.scenario.objectCount(), cpuMs.size(),
                frameMs.mean(), frameMs.percentile(0.5), frameMs.percentile(0.95), frameMs.percentile(0.99),
                cpuMs.mean(), gpuMs.size() > 0 ? gpuMs.mean() : -1,
                drawCalls / f, primitives / f, vertices / f, textureBinds / f, uploadedBytes / f / 1024.0);
    }

    private void report() {
        String gpu = glGetString(GL_RENDERER);
        StringBuilder table = new StringBuilder();
        table.append(String.format(Locale.ROOT, "%nRender benchmark: %s | LWJGL %s | Java %s | %s %s%n",
                gpu, org.lwjgl.Version.getVersion(), System.getProperty("java.version"),
                System.getProperty("os.name"), System.getProperty("os.arch")));
        table.append(String.format(Locale.ROOT, "%-26s %-9s %8s %7s %8s %8s %8s %8s %8s %8s %9s %9s %8s %10s%n",
                "scenario", "renderer", "objects", "frames", "fps", "frame ms", "p95 ms", "p99 ms", "cpu ms",
                "gpu ms", "draws/f", "prims/f", "binds/f", "upload KB/f"));
        for (Result r : results) {
            table.append(String.format(Locale.ROOT,
                    "%-26s %-9s %8d %7d %8.1f %8.3f %8.3f %8.3f %8.3f %8s %9.1f %9.0f %8.1f %10.1f%n",
                    r.name, r.renderer, r.objects, r.frames, r.meanFrameMs > 0 ? 1000 / r.meanFrameMs : 0, r.meanFrameMs,
                    r.p95FrameMs, r.p99FrameMs, r.meanCpuMs, r.meanGpuMs >= 0 ? String.format(Locale.ROOT, "%.3f",
                            r.meanGpuMs) : "n/a", r.drawCallsPerFrame, r.primitivesPerFrame, r.textureBindsPerFrame,
                    r.uploadKbPerFrame));
        }
        log.info(table.toString());
        writeCsv(gpu);
    }

    private void writeCsv(String gpu) {
        try {
            Path dir = Path.of("build", "benchmarks");
            Files.createDirectories(dir);
            String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            Path file = dir.resolve("render-" + stamp + ".csv");
            try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(file))) {
                out.println("renderer,gpu,lwjgl,scenario,objects,frames,mean_frame_ms,p50_frame_ms,p95_frame_ms,"
                        + "p99_frame_ms,mean_cpu_ms,mean_gpu_ms,draw_calls_per_frame,primitives_per_frame,"
                        + "vertices_per_frame,texture_binds_per_frame,upload_kb_per_frame");
                for (Result r : results) {
                    out.printf(Locale.ROOT,
                            "%s,\"%s\",%s,%s,%d,%d,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.2f,%.1f,%.1f,%.2f,%.2f%n",
                            r.renderer, gpu, org.lwjgl.Version.getVersion(), r.name, r.objects, r.frames,
                            r.meanFrameMs, r.p50FrameMs, r.p95FrameMs, r.p99FrameMs, r.meanCpuMs, r.meanGpuMs,
                            r.drawCallsPerFrame, r.primitivesPerFrame, r.verticesPerFrame, r.textureBindsPerFrame,
                            r.uploadKbPerFrame);
                }
            }
            log.info("Results written to {0}", file.toAbsolutePath());
        } catch (IOException e) {
            log.error("Unable to write benchmark results.", e);
        }
    }

    // endregion

    // region scenarios

    private abstract static class Scenario {
        final String name;

        Scenario(String name) {
            this.name = name;
        }

        abstract void setup(Random random);

        abstract int objectCount();

        void update(float dt) {
        }

        abstract void draw(SpriteBatch batch);
    }

    /** Moving sprites; texture count, depth levels and rotation vary per scenario. */
    private final class SpriteScenario extends Scenario {
        private final int count, textureCount, depthLevels;
        private final boolean rotate;
        private float[] x, y, vx, vy, rotation;
        private int[] texture, depth;
        private final Vector2 position = new Vector2();
        private final Vector2 anchor = new Vector2(0.5f, 0.5f);

        SpriteScenario(String name, int count, int textureCount, int depthLevels, boolean rotate) {
            super(name);
            this.count = count;
            this.textureCount = textureCount;
            this.depthLevels = depthLevels;
            this.rotate = rotate;
        }

        @Override
        void setup(Random random) {
            x = new float[count];
            y = new float[count];
            vx = new float[count];
            vy = new float[count];
            rotation = new float[count];
            texture = new int[count];
            depth = new int[count];
            for (int i = 0; i < count; i++) {
                x[i] = random.nextFloat() * WIDTH;
                y[i] = random.nextFloat() * HEIGHT;
                vx[i] = (random.nextFloat() - 0.5f) * 200;
                vy[i] = (random.nextFloat() - 0.5f) * 200;
                rotation[i] = random.nextFloat() * 6.28f;
                texture[i] = random.nextInt(textureCount); // random order: worst case for texture switching
                depth[i] = random.nextInt(depthLevels);
            }
        }

        @Override
        int objectCount() {
            return count;
        }

        @Override
        void update(float dt) {
            for (int i = 0; i < count; i++) {
                x[i] += vx[i] * dt;
                y[i] += vy[i] * dt;
                if (x[i] < 0 || x[i] > WIDTH) vx[i] = -vx[i];
                if (y[i] < 0 || y[i] > HEIGHT) vy[i] = -vy[i];
                if (rotate) rotation[i] += dt;
            }
        }

        @Override
        void draw(SpriteBatch batch) {
            batch.begin(camera.getViewMatrix());
            for (int i = 0; i < count; i++) {
                position.set(x[i], y[i]);
                batch.draw(textures[texture[i]], position, null, Color.WHITE, anchor, 1f, 1f,
                        rotate ? rotation[i] : 0f, depth[i]);
            }
            batch.end();
        }
    }

    /** Many short strings (bitmap font); measures glyph throughput. */
    private final class TextScenario extends Scenario {
        private final int lines;
        private String[] text;
        private final Vector2 position = new Vector2();

        TextScenario(String name, int lines) {
            super(name);
            this.lines = lines;
        }

        @Override
        void setup(Random random) {
            text = new String[lines];
            for (int i = 0; i < lines; i++) {
                text[i] = "Line " + i + ": the quick brown fox jumps";
            }
        }

        @Override
        int objectCount() {
            return lines;
        }

        @Override
        void draw(SpriteBatch batch) {
            batch.begin(camera.getViewMatrix());
            for (int i = 0; i < lines; i++) {
                position.set((i % 4) * 320f, (i / 4 % 40) * 18f);
                batch.drawText(font, text[i], position, Color.WHITE);
            }
            batch.end();
        }
    }

    /**
     * Sprite, label, sprite, label... in submission order. This is the case where the task/urp pipeline switched
     * renderers on every item; kept here so v2 can be compared on the same workload.
     */
    private final class InterleavedScenario extends Scenario {
        private final int count;
        private float[] x, y;
        private String[] labels;
        private final Vector2 position = new Vector2();

        InterleavedScenario(String name, int count) {
            super(name);
            this.count = count;
        }

        @Override
        void setup(Random random) {
            x = new float[count];
            y = new float[count];
            labels = new String[count];
            for (int i = 0; i < count; i++) {
                x[i] = random.nextFloat() * (WIDTH - 40);
                y[i] = random.nextFloat() * (HEIGHT - 40);
                labels[i] = "#" + i;
            }
        }

        @Override
        int objectCount() {
            return count * 2;
        }

        @Override
        void draw(SpriteBatch batch) {
            batch.begin(camera.getViewMatrix());
            for (int i = 0; i < count; i++) {
                position.set(x[i], y[i]);
                batch.draw(textures[i % 4], position, Color.WHITE);
                position.set(x[i] + 4, y[i] + 4);
                batch.drawText(font, labels[i], position, Color.WHITE);
            }
            batch.end();
        }
    }

    // endregion

    // region measurement helpers

    private record Run(Scenario scenario, String rendererName, SpriteBatch renderer) {
    }

    private record Result(String name, String renderer, int objects, long frames, double meanFrameMs, double p50FrameMs,
                          double p95FrameMs, double p99FrameMs, double meanCpuMs, double meanGpuMs,
                          double drawCallsPerFrame, double primitivesPerFrame, double verticesPerFrame,
                          double textureBindsPerFrame, double uploadKbPerFrame) {
    }

    /** Growable list of doubles (no boxing while measuring). */
    private static final class Samples {
        private double[] values = new double[4096];
        private int size;

        void add(double v) {
            if (size == values.length) {
                values = Arrays.copyOf(values, size * 2);
            }
            values[size++] = v;
        }

        void clear() {
            size = 0;
        }

        int size() {
            return size;
        }

        double mean() {
            if (size == 0) return 0;
            double sum = 0;
            for (int i = 0; i < size; i++) sum += values[i];
            return sum / size;
        }

        double percentile(double p) {
            if (size == 0) return 0;
            double[] sorted = Arrays.copyOf(values, size);
            Arrays.sort(sorted);
            return sorted[Math.min(size - 1, (int) Math.ceil(p * size) - 1)];
        }
    }

    // endregion

    public static void main(String[] args) {
        var settings = new WindowSettings("Pixel - Render Benchmark", WIDTH, HEIGHT);
        settings.setWindowResizable(false);
        settings.setMultisampling(0);
        settings.setVsync(false);       // measure the renderer, not the display
        settings.setTargetFps(0);       // uncapped
        settings.setIdleThrottle(false); // keep full speed even if the window is not focused
        settings.setAutoClear(true);

        DesktopLauncher.run(new RenderBenchmark(), settings);
    }
}
