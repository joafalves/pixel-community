package org.pixel.content;

import org.pixel.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MobileResourceLoader implements ResourceLoader {

    @Override
    public byte[] load(String path) {
        // Basic security check to prevent Directory Traversal attacks.
        if (path.contains("..") || path.startsWith("/") || path.contains("\\\\")) {
            throw new SecurityException("Invalid file path.");
        }

        try (InputStream fileStream = FileUtils.loadAsset(path)) {
            if (fileStream == null) {
                return null;
            }

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384]; // Use a buffer size of 16KB.
            while ((nRead = fileStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            buffer.flush();
            return buffer.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
