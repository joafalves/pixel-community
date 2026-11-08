package org.pixel.content;

import org.pixel.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

public class MobileResourceLoader implements ResourceLoader {

    private final android.content.Context context;

    /**
     * Constructor.
     *
     * @param context The Android context whose assets are loaded.
     */
    public MobileResourceLoader(android.content.Context context) {
        this.context = context;
    }

    private static final int BUFFER_SIZE = 16384; // 16KB buffer

    @Override
    public byte[] load(String path) throws IOException {
        // Security: Use canonical path to resolve ".." and other relative path tricks.
        String canonicalPath = Paths.get(path).normalize().toString();

        if (!canonicalPath.equals(path) || canonicalPath.contains("..") || canonicalPath.startsWith("/") || canonicalPath.contains("\\\\")) {
            throw new SecurityException("Invalid or potentially malicious file path: " + path);
        }

        try (InputStream fileStream = FileUtils.loadAsset(context, canonicalPath)) {
            if (fileStream == null) {
                throw new FileNotFoundException("File not found: " + canonicalPath);
            }

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int bytesRead;
            byte[] data = new byte[BUFFER_SIZE];
            while ((bytesRead = fileStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, bytesRead);
            }

            buffer.flush();
            return buffer.toByteArray();

        }
    }
}