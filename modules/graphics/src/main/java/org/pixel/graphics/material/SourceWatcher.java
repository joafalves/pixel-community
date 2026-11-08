package org.pixel.graphics.material;

import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Reloads a shader source from a file when it changes on disk, for editing shaders while the game runs:
 * <pre>
 * watcher = SourceWatcher.effect(Path.of("src/main/resources/shaders/water.glsl"), water);
 * ...
 * watcher.poll(); // every frame (e.g. in update()); checks the file a few times per second
 * </pre>
 * Polling, no threads: the callback runs on the caller's thread, so it can change materials directly. A file that
 * can't be read, or contents the callback rejects (an editor caught mid-save), are reported once and checked again on
 * the next change; the time and the size of the file both count as changes.
 */
public final class SourceWatcher {

    private static final Logger log = LoggerFactory.getLogger(SourceWatcher.class);
    private static final long INTERVAL_NANOS = 250_000_000L;

    private final Path file;
    private final Consumer<String> onChange;
    private long lastModified;
    private long lastSize = -1;
    private long nextCheck;
    private boolean failing;

    /**
     * Watch a file. The current contents count as already loaded: {@code onChange} runs on the first change.
     *
     * @param file     The file.
     * @param onChange Receives the new contents.
     */
    public SourceWatcher(Path file, Consumer<String> onChange) {
        if (file == null || onChange == null) {
            throw new IllegalArgumentException("File and callback cannot be null.");
        }
        this.file = file;
        this.onChange = onChange;
        this.lastModified = modified();
        this.lastSize = size();
        this.nextCheck = System.nanoTime() + INTERVAL_NANOS;
    }

    /**
     * Watch the effect function of an effect material.
     */
    public static SourceWatcher effect(Path file, Material material) {
        if (material.getKind() != Material.Kind.EFFECT) {
            throw new IllegalArgumentException("Not an effect material: " + material);
        }
        return new SourceWatcher(file, material::setSource);
    }

    /**
     * Check the file (at most four times per second) and call back if it changed.
     *
     * @return Whether the callback ran.
     */
    public boolean poll() {
        long now = System.nanoTime();
        if (now - nextCheck < 0) {
            return false;
        }
        nextCheck = now + INTERVAL_NANOS;
        long modified = modified();
        long size = size();
        if (modified == Long.MIN_VALUE || (modified == lastModified && size == lastSize)) {
            return false;
        }
        String source;
        try {
            source = Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            reportFailure(e.toString());
            return false;
        }
        lastModified = modified;
        lastSize = size;
        try {
            onChange.accept(source);
        } catch (RuntimeException e) {
            reportFailure(e.getMessage()); // e.g. a half-written file: the completing write is another change
            return false;
        }
        failing = false;
        return true;
    }

    public Path getFile() {
        return file;
    }

    private long modified() {
        try {
            return Files.getLastModifiedTime(file).toMillis();
        } catch (IOException e) {
            reportFailure(e.toString());
            return Long.MIN_VALUE;
        }
    }

    private long size() {
        try {
            return Files.size(file);
        } catch (IOException e) {
            return -1;
        }
    }

    private void reportFailure(String reason) {
        if (!failing) {
            failing = true;
            log.warn("Unable to load {0}: {1}", file.toAbsolutePath(), reason);
        }
    }
}
