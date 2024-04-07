package org.pixel.io;

import android.content.Context;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileUtils {
    private static final Logger log = LoggerFactory.getLogger(FileUtils.class);

    /**
     * Load asset file.
     *
     * @param context The android context.
     * @param path    The asset file path.
     * @return The file content.
     */
    public static String loadAsset(Context context, String path) {
        StringBuilder shaderCode = new StringBuilder();
        try (InputStream is = context.getAssets().open(path)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                shaderCode.append(line).append('\n');
            }

        } catch (IOException e) {
            log.error("Error loading asset: " + path, e);
            return null;
        }
        return shaderCode.toString();
    }
}
