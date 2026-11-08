package org.pixel.demo.concept.spaceshooter.debug;

import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.system.MemoryUtil;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.core.GameContext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Play-testing without hands (desktop): with {@code -Dspaceshooter.capture=<dir>} the game saves screenshots at the
 * given seconds ({@code -Dspaceshooter.captureAt=2,8,15}, default 3) and exits after the last one. Combine with
 * {@code -Dspaceshooter.autoplay=true} (the autopilot plays a run), {@code -Dspaceshooter.wave=5} and
 * {@code -Dspaceshooter.lives=1}.
 */
public final class Capture {

    private static final Logger log = LoggerFactory.getLogger(Capture.class);

    private final Path directory;
    private final float[] times;
    private final com.sun.management.ThreadMXBean threads =
            (com.sun.management.ThreadMXBean) java.lang.management.ManagementFactory.getThreadMXBean();
    private int next, frames;
    private float clock;
    private long allocatedAtLastShot = -1;

    private Capture(Path directory, float[] times) {
        this.directory = directory;
        this.times = times;
    }

    /**
     * @return The capture configured by system properties, or {@code null}.
     */
    public static Capture fromSystemProperties() {
        String dir = System.getProperty("spaceshooter.capture");
        if (dir == null || dir.isBlank()) {
            return null;
        }
        String[] parts = System.getProperty("spaceshooter.captureAt", "3").split(",");
        float[] times = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            times[i] = Float.parseFloat(parts[i].trim());
        }
        return new Capture(Path.of(dir), times);
    }

    public static boolean autoplay() {
        return Boolean.getBoolean("spaceshooter.autoplay");
    }

    /** Lives to start runs with ({@code -Dspaceshooter.lives}), or 0 for the default. */
    public static int lives() {
        return Math.max(0, Integer.getInteger("spaceshooter.lives", 0));
    }

    public static int firstWave() {
        return Math.max(1, Integer.getInteger("spaceshooter.wave", 1));
    }

    /**
     * Call at the end of {@code draw()}, after the pipeline rendered.
     *
     * @return Whether every screenshot was taken (the game should exit).
     */
    public boolean afterDraw(GameContext context, float dt) {
        clock += Math.min(dt, 0.25f); // game time: the first frame's delta includes loading
        frames++;
        if (next < times.length && clock >= times[next]) {
            // the game thread's allocations since the previous screenshot (the engine aims for none per frame)
            long allocated = threads.getCurrentThreadAllocatedBytes();
            if (allocatedAtLastShot >= 0) {
                log.info("Allocated {0} bytes per frame over {1} frames", (allocated - allocatedAtLastShot) / frames,
                        frames);
            }
            Path file = directory.resolve(String.format("spaceshooter-%02d.png", next + 1));
            try {
                save(context, file);
                log.info("Saved {0}", file.toAbsolutePath());
            } catch (IOException e) {
                log.error("Unable to save {0}", file, e);
            }
            next++;
            frames = 0;
            allocatedAtLastShot = threads.getCurrentThreadAllocatedBytes();
        }
        return next >= times.length;
    }

    private static void save(GameContext context, Path file) throws IOException {
        context.devTools().render(); // drawn after draw() by the host: include it when shown
        int width = context.window().getWidth(), height = context.window().getHeight();
        byte[] rgba = context.graphics().readPixels(0, 0, width, height, true);
        Files.createDirectories(file.toAbsolutePath().getParent());
        ByteBuffer buffer = MemoryUtil.memAlloc(rgba.length);
        try {
            buffer.put(rgba).flip();
            STBImageWrite.stbi_flip_vertically_on_write(true);
            if (!STBImageWrite.stbi_write_png(file.toString(), width, height, 4, buffer, width * 4)) {
                throw new IOException("Unable to write " + file);
            }
        } finally {
            MemoryUtil.memFree(buffer);
        }
    }
}
