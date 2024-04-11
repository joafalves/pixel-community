package org.pixel.io;

import android.content.Context;
import org.pixel.commons.GameContext;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.core.MobileTags;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileUtils {
    private static final Logger log = LoggerFactory.getLogger(FileUtils.class);

    /**
     * Load asset file using the default context. This method can only be used after the game has been initialized.
     *
     * @param path The asset file path.
     * @return The file content.
     */
    public static InputStream loadAsset(String path) {
        return loadAsset(GameContext.getData().get(MobileTags.ACTIVE_CONTEXT, Context.class), path);
    }

    /**
     * Load asset file.
     *
     * @param ctx  The android context.
     * @param path The asset file path.
     * @return The file content.
     */
    public static InputStream loadAsset(Context ctx, String path) {
        if (ctx == null) {
            log.error("No active context found. Unable to load asset: " + path);
            return null;
        }

        try {
            return ctx.getAssets().open(path);
        } catch (IOException e) {
            log.error("Error loading asset: " + path, e);
            return null;
        }
    }

    /**
     * Load asset file using the default context. This method can only be used after the game has been initialized.
     *
     * @param path The asset file path.
     * @return The file content.
     */
    public static String loadTextAsset(String path) {
        return loadTextAsset(GameContext.getData().get(MobileTags.ACTIVE_CONTEXT, Context.class), path);
    }

    /**
     * Load asset file.
     *
     * @param ctx  The android context.
     * @param path The asset file path.
     * @return The file content.
     */
    public static String loadTextAsset(Context ctx, String path) {
        if (ctx == null) {
            log.error("No active context found. Unable to load asset: " + path);
            return null;
        }

        StringBuilder shaderCode = new StringBuilder();
        try (InputStream is = ctx.getAssets().open(path)) {
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
