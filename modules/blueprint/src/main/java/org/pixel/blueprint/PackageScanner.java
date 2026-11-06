package org.pixel.blueprint;

import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Finds classes in a package by scanning the classpath. Supports both exploded directories (IDE / Gradle runs) and
 * jar files (packaged games), across every classpath root that contains the package.
 */
public class PackageScanner {

    private static final Logger log = LoggerFactory.getLogger(PackageScanner.class);
    private static final String CLASS_SUFFIX = ".class";

    public static Set<Class<?>> findClassesWithAnnotation(String packageName, Class<?> annotationClass) {
        @SuppressWarnings("unchecked")
        Class<? extends Annotation> annotation = (Class<? extends Annotation>) annotationClass;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        Set<Class<?>> classes = new LinkedHashSet<>();
        for (String className : findClassNames(classLoader, packageName)) {
            try {
                // don't run static initializers just to inspect annotations
                Class<?> clazz = Class.forName(className, false, classLoader);
                if (clazz.isAnnotationPresent(annotation)) {
                    classes.add(clazz);
                }
            } catch (ClassNotFoundException | LinkageError e) {
                log.error("Unable to load class {0} while scanning package {1}.", className, packageName, e);
            }
        }
        return classes;
    }

    /**
     * Find the fully qualified names of all classes (including nested ones) in the given package and sub-packages.
     *
     * @param classLoader The class loader used to locate the package resources.
     * @param packageName The package name (e.g. "org.example.game").
     * @return The fully qualified class names.
     */
    static Set<String> findClassNames(ClassLoader classLoader, String packageName) {
        String packagePath = packageName.replace('.', '/');
        Set<String> classNames = new LinkedHashSet<>();
        try {
            Enumeration<URL> resources = classLoader.getResources(packagePath);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                switch (url.getProtocol()) {
                    case "file" -> scanDirectory(Paths.get(url.toURI()), packageName, classNames);
                    case "jar" -> {
                        JarURLConnection connection = (JarURLConnection) url.openConnection();
                        connection.setUseCaches(false);
                        try (JarFile jarFile = connection.getJarFile()) {
                            scanJar(jarFile, packagePath, classNames);
                        }
                    }
                    default -> log.warn("Unsupported classpath resource protocol: {0} ({1}).", url.getProtocol(), url);
                }
            }
        } catch (IOException | URISyntaxException e) {
            log.error("Exception caught while scanning package {0}!", packageName, e);
        }
        return classNames;
    }

    static void scanDirectory(Path root, String packageName, Set<String> classNames) throws IOException {
        try (var pathStream = Files.walk(root)) {
            pathStream.filter(Files::isRegularFile)
                    .map(file -> root.relativize(file).toString())
                    .filter(PackageScanner::isScannableClassFile)
                    .forEach(relative -> classNames.add(packageName + '.' + toClassName(relative)));
        }
    }

    static void scanJar(JarFile jarFile, String packagePath, Set<String> classNames) {
        String prefix = packagePath.endsWith("/") ? packagePath : packagePath + '/';
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (!entry.isDirectory() && name.startsWith(prefix) && isScannableClassFile(name)) {
                classNames.add(toClassName(name));
            }
        }
    }

    private static boolean isScannableClassFile(String path) {
        return path.endsWith(CLASS_SUFFIX)
                && !path.endsWith("module-info.class")
                && !path.endsWith("package-info.class");
    }

    private static String toClassName(String path) {
        return path.substring(0, path.length() - CLASS_SUFFIX.length())
                .replace('\\', '/')
                .replace('/', '.');
    }
}
