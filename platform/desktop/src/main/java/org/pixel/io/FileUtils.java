/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.io;

import org.lwjgl.system.MemoryStack;
import org.pixel.commons.data.ImageData;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.commons.util.FileHelper;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.lwjgl.BufferUtils.createByteBuffer;
import static org.lwjgl.stb.STBImage.stbi_failure_reason;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;

public class FileUtils {

    private static final Logger log = LoggerFactory.getLogger(FileUtils.class);

    /**
     * Load image file.
     *
     * @param filepath The image file path.
     * @return The image data.
     */
    public static ImageData loadImage(String filepath) {
        byte[] data;
        try {
            data = loadFile(filepath);
        } catch (IOException e) {
           log.error("Exception caught while reading file {0}: {1}", filepath, e.getMessage(), e);
           return null;
        }
        if (data == null) {
            log.warn("Unable to load image due to IO failure (cannot read file from {0}).", filepath);
            return null;
        }

        ByteBuffer rawBuffer = createByteBuffer(data.length);
        rawBuffer.put(data).flip(); // reset position to 0

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            // load image data from buffer
            ByteBuffer imageData = stbi_load_from_memory(rawBuffer, w, h, comp, 4);
            if (imageData == null) {
                throw new RuntimeException("Failed to process texture file: " + stbi_failure_reason());
            }

            return new ImageData(imageData, w.get(), h.get());
        }
    }

    /**
     * Load file as byte buffer.
     *
     * @param filepath The file path (relative paths allowed).
     * @return The byte buffer.
     */
    public static byte[] loadFile(String filepath) throws IOException {
        Path path = Paths.get(filepath);

        // Handle relative paths
        if (!path.isAbsolute()) {
            try (InputStream in = FileHelper.class.getClassLoader().getResourceAsStream(filepath)) {
                if (in == null) {
                    log.warn("Unable to load local resource file {0}.", filepath);
                    return null;
                }
                return in.readAllBytes();
            }
        }

        // Handle absolute paths
        if (Files.isReadable(path)) {
            try (SeekableByteChannel channel = Files.newByteChannel(path);
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                ByteBuffer buffer = ByteBuffer.allocate(8192); // 8KB buffer
                while (channel.read(buffer) > 0) {
                    buffer.flip(); // Switch buffer from write to read mode
                    outputStream.write(buffer.array(), 0, buffer.remaining());
                    buffer.clear(); // Clear buffer for the next read
                }

                return outputStream.toByteArray();

            } catch (IOException e) {
                log.error("Exception caught while reading file {0}: {1}", filepath, e.getMessage(), e);
            }
        } else {
            log.warn("File {0} is not readable or does not exist.", filepath);
        }

        return null;
    }

    /**
     * Load file content.
     *
     * @param filepath The file path.
     * @return The file content.
     */
    public static String loadFileString(String filepath) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (InputStream resourceStream = loader.getResourceAsStream(filepath)) {
            return new BufferedReader(new InputStreamReader(Objects.requireNonNull(resourceStream)))
                    .lines().collect(Collectors.joining(System.lineSeparator()));

        } catch (IOException e) {
            log.error("Exception caught!", e);
        }

        return null;
    }
}
