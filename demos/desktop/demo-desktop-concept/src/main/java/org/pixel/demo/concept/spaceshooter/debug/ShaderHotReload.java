package org.pixel.demo.concept.spaceshooter.debug;

import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.demo.concept.spaceshooter.assets.Assets;
import org.pixel.demo.concept.spaceshooter.fx.Shockwave;
import org.pixel.graphics.material.SourceWatcher;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Dev mode: reload the game's shaders when their source files change, while the game runs (edit
 * {@code resources/spaceshooter/shaders/*.glsl} and save). Only when the sources are found on disk (running from the
 * repository); a shader that fails to compile keeps the previous version and logs the error.
 */
public final class ShaderHotReload {

    private static final Logger log = LoggerFactory.getLogger(ShaderHotReload.class);
    private static final String[] SOURCE_ROOTS = {
            "src/main/resources/", // working directory: the module (Gradle)
            "demos/desktop/demo-desktop-concept/src/main/resources/" // working directory: the repository (IDE)
    };

    private final SourceWatcher hitFlash, shockwave;

    private ShaderHotReload(SourceWatcher hitFlash, SourceWatcher shockwave) {
        this.hitFlash = hitFlash;
        this.shockwave = shockwave;
    }

    /**
     * @return The reloader, or {@code null} when the shader sources aren't on disk.
     */
    public static ShaderHotReload create(Assets assets, Shockwave shockwaveEffect) {
        for (String root : SOURCE_ROOTS) {
            Path dir = Path.of(root, Assets.ROOT);
            if (Files.isRegularFile(dir.resolve(Assets.HIT_FLASH_SHADER))) {
                log.info("Watching shaders in {0}", dir.toAbsolutePath());
                return new ShaderHotReload(
                        SourceWatcher.effect(dir.resolve(Assets.HIT_FLASH_SHADER), assets.hitFlash),
                        new SourceWatcher(dir.resolve(Assets.SHOCKWAVE_SHADER), shockwaveEffect::setSource));
            }
        }
        return null;
    }

    public void poll() {
        hitFlash.poll();
        shockwave.poll();
    }
}
