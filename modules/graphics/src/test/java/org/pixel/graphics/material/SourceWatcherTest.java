package org.pixel.graphics.material;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SourceWatcherTest {

    private static final String V1 = "vec4 effect(vec4 color, vec2 uv, Fragment f) { return color; }";
    private static final String V2 = "vec4 effect(vec4 color, vec2 uv, Fragment f) { return color * 0.5; }";

    @TempDir
    Path dir;

    /** Poll until the throttle lets a check through (a quarter of a second at most). */
    private static boolean pollUntilChecked(SourceWatcher watcher) throws InterruptedException {
        long end = System.nanoTime() + 1_000_000_000L;
        while (System.nanoTime() < end) {
            if (watcher.poll()) {
                return true;
            }
            Thread.sleep(20);
        }
        return false;
    }

    @Test
    void reloadsWhenTheFileChangesOnly() throws IOException, InterruptedException {
        Path file = dir.resolve("flash.glsl");
        Files.writeString(file, V1);
        List<String> loaded = new ArrayList<>();
        SourceWatcher watcher = new SourceWatcher(file, loaded::add);

        assertFalse(pollUntilChecked(watcher), "the current contents count as loaded");
        Files.writeString(file, V2);
        Files.setLastModifiedTime(file, FileTime.fromMillis(Files.getLastModifiedTime(file).toMillis() + 2000));
        assertTrue(pollUntilChecked(watcher));
        assertEquals(List.of(V2), loaded);
    }

    @Test
    void aHalfWrittenFileIsReportedAndTheCompletingWriteReloads() throws IOException, InterruptedException {
        Path file = dir.resolve("flash.glsl");
        Files.writeString(file, V1);
        Material material = Material.effect(V1);
        SourceWatcher watcher = SourceWatcher.effect(file, material);
        Files.writeString(file, ""); // an editor truncating before it writes: rejected, not thrown
        assertFalse(pollUntilChecked(watcher));
        assertEquals(V1, material.getSource());
        Files.writeString(file, V2); // possibly within the same millisecond: the size changed
        assertTrue(pollUntilChecked(watcher));
        assertEquals(V2, material.getSource());
    }

    @Test
    void updatesAnEffectMaterialAndSurvivesAMissingFile() throws IOException, InterruptedException {
        Path file = dir.resolve("flash.glsl");
        Files.writeString(file, V1);
        Material material = Material.effect(V1);
        SourceWatcher watcher = SourceWatcher.effect(file, material);
        Files.delete(file);
        assertFalse(pollUntilChecked(watcher));
        Files.writeString(file, V2);
        assertTrue(pollUntilChecked(watcher));
        assertEquals(V2, material.getSource());
        assertThrows(IllegalArgumentException.class,
                () -> SourceWatcher.effect(file, Material.raw("void main() {}", "void main() {}")));
    }
}
