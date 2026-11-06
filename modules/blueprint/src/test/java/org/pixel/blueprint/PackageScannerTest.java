package org.pixel.blueprint;

import org.junit.jupiter.api.Test;
import org.pixel.blueprint.annotation.Blueprint;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class PackageScannerTest {

    @Test
    void scansClassesInsideJarFiles(@TempDir Path tempDir) throws Exception {
        Path jar = tempDir.resolve("game.jar");
        try (JarOutputStream out = new JarOutputStream(new FileOutputStream(jar.toFile()))) {
            for (String entry : new String[]{
                    "com/game/", "com/game/Main.class", "com/game/sub/Service.class", "com/game/Main$Inner.class",
                    "com/game/package-info.class", "com/game/config.json", "com/gameother/Other.class"}) {
                out.putNextEntry(new JarEntry(entry));
                out.closeEntry();
            }
        }

        Set<String> names = new LinkedHashSet<>();
        try (JarFile jarFile = new JarFile(jar.toFile())) {
            PackageScanner.scanJar(jarFile, "com/game", names);
        }

        assertEquals(Set.of("com.game.Main", "com.game.sub.Service", "com.game.Main$Inner"), names);
    }

    @Test
    void scansClassesInDirectoriesOnly(@TempDir Path tempDir) throws Exception {
        Path pkg = tempDir.resolve("a");
        java.nio.file.Files.createDirectories(pkg.resolve("b"));
        java.nio.file.Files.createFile(pkg.resolve("A.class"));
        java.nio.file.Files.createFile(pkg.resolve("b").resolve("B.class"));
        java.nio.file.Files.createFile(pkg.resolve("readme.txt"));
        java.nio.file.Files.createFile(pkg.resolve("package-info.class"));

        Set<String> names = new LinkedHashSet<>();
        PackageScanner.scanDirectory(pkg, "x.a", names);

        assertEquals(Set.of("x.a.A", "x.a.b.B"), names);
    }

    @Test
    void findsAnnotatedClassesOnTheClasspath() {
        // the other blueprint tests declare @Blueprint classes in this package (test classes directory)
        Set<Class<?>> found = PackageScanner.findClassesWithAnnotation("org.pixel.blueprint", Blueprint.class);
        assertFalse(found.isEmpty());
        assertTrue(found.stream().allMatch(c -> c.isAnnotationPresent(Blueprint.class)));
    }
}
