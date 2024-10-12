/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.io;

import org.lwjgl.system.MemoryStack;
import org.pixel.commons.data.ImageData;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
        byte[] data = loadFile(filepath);
        if (data == null) {
            log.warn("Unable to load image due to IO failure (cannot read file from '{}').", filepath);
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

            return ImageData.builder()
                    .data(imageData)
                    .width(w.get(0))
                    .height(h.get(0))
                    .build();
        }
    }

    /**
     * Load file as byte buffer.
     *
     * @param filepath The file path (relative paths allowed).
     * @return The byte buffer.
     */
    public static byte[] loadFile(String filepath) {
        Path path = Paths.get(filepath);
        if (!path.isAbsolute()) {
            InputStream in = org.pixel.commons.util.FileUtils.class.getClassLoader().getResourceAsStream(filepath);
            if (in == null) {
                log.warn("Unable to load local resource file '{}'.", filepath);
                return null;
            }

            try {
                return in.readAllBytes();

            } catch (IOException e) {
                log.error("Exception caught while loading relative path resource!", e);
            }

            return null;
        }

        if (Files.isReadable(path)) {
            try (SeekableByteChannel fc = Files.newByteChannel(path)) {
                ByteBuffer buffer = createByteBuffer((int) fc.size());
                while (fc.read(buffer) != -1) ; // write into our buffer
                return buffer.array();

            } catch (IOException e) {
                log.error("Exception caught!", e);
            }
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
