package org.pixel.demo.learning.common;

import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.system.MemoryUtil;
import org.pixel.core.Game;

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
     * Capture the back buffer of the game window (call at the end of {@code draw()}).
     *
     * @param game The game.
     * @param file The PNG file to write.
     * @throws IOException If the directory cannot be created.
     */
    public static void capture(Game game, Path file) throws IOException {
        int width = game.getWindowManager().getWindowWidth();   // framebuffer pixels (HiDPI aware)
        int height = game.getWindowManager().getWindowHeight();
        writePng(file, game.getGraphicsDevice().readPixels(0, 0, width, height, true), width, height);
    }

    /**
     * Write RGBA pixels read from OpenGL (rows bottom-up) as a PNG.
     */
    public static void writePng(Path file, byte[] rgba, int width, int height) throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
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
