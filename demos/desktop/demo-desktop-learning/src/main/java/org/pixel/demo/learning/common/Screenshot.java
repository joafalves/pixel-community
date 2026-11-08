package org.pixel.demo.learning.common;

import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.system.MemoryUtil;
import org.pixel.core.GameContext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Saves the current back buffer (or given RGBA pixels) as a PNG. Used by demos run with
 * {@code -Dpixel.screenshot=<file.png>} and by the render parity check.
 */
public final class Screenshot {

    private Screenshot() {
    }

    /**
     * Capture the back buffer of the game window (call at the end of {@code draw()}). The dev tools are drawn by the
     * host after {@code draw()}, so when they are shown they are drawn first here to appear in the image.
     *
     * @param context The game context.
     * @param file    The PNG file to write.
     * @throws IOException If the directory cannot be created.
     */
    public static void capture(GameContext context, Path file) throws IOException {
        context.devTools().render();
        int width = context.window().getWidth();   // framebuffer pixels (HiDPI aware)
        int height = context.window().getHeight();
        writePng(file, context.graphics().readPixels(0, 0, width, height, true), width, height);
    }

    /**
     * Write RGBA pixels read from OpenGL (rows bottom-up) as a PNG.
     */
    public static void writePng(Path file, byte[] rgba, int width, int height) throws IOException {
        writePng(file, rgba, width, height, true);
    }

    /**
     * Write RGBA pixels as a PNG.
     *
     * @param bottomUp Whether the rows go from bottom to top (screen reads) or top to bottom (render targets).
     */
    public static void writePng(Path file, byte[] rgba, int width, int height, boolean bottomUp)
            throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        ByteBuffer buffer = MemoryUtil.memAlloc(rgba.length);
        try {
            buffer.put(rgba).flip();
            STBImageWrite.stbi_flip_vertically_on_write(bottomUp);
            if (!STBImageWrite.stbi_write_png(file.toString(), width, height, 4, buffer, width * 4)) {
                throw new IOException("Unable to write " + file);
            }
        } finally {
            MemoryUtil.memFree(buffer);
        }
    }
}
