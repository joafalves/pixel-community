package org.pixel.graphics.opengl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.stb.STBImageWrite.stbi_flip_vertically_on_write;
import static org.lwjgl.stb.STBImageWrite.stbi_write_png;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GLTextureExporter {

    /**
     * Exports a texture to a PNG file.
     *
     * @param textureID The OpenGL texture ID.
     * @param width     The width of the texture.
     * @param height    The height of the texture.
     * @param filePath  The file path to save the PNG file.
     */
    public static void png(int textureID, int width, int height, String filePath) {
        glBindTexture(GL_TEXTURE_2D, textureID);

        int channels = 4; // Assuming RGBA

        // Allocate a buffer to hold the pixel data
        ByteBuffer buffer = MemoryUtil.memAlloc(width * height * channels);
        glGetTexImage(GL_TEXTURE_2D, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

        // Settings & write the PNG file
        stbi_flip_vertically_on_write(false);
        stbi_write_png(filePath, width, height, channels, buffer, width * channels);

        // Free the buffer
        MemoryUtil.memFree(buffer);
    }
}
