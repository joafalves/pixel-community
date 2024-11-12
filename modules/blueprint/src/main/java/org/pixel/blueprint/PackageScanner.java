package org.pixel.blueprint;

import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class PackageScanner {

    private static final Logger log = LoggerFactory.getLogger(PackageScanner.class);

    public static Set<Class<?>> findClassesWithAnnotation(String packageName, Class<?> annotationClass) {
        Set<Class<?>> classes = new HashSet<>();
        String packagePath = packageName.replace('.', '/');
        try {
            URL packageUrl = Thread.currentThread().getContextClassLoader().getResource(packagePath);
            if (packageUrl != null) {
                Path path = Paths.get(packageUrl.toURI());
                try (var pathStream = Files.walk(path)) {
                    pathStream.filter(Files::isRegularFile).forEach(file -> {
                        String className = file.toString()
                                .replace(path + File.separator, "") // Use File.separator for cross-platform compatibility
                                .replace(".class", "") // Remove the .class extension
                                .replace(File.separator, "."); // Replace path separators with dots

                        try {
                            Class<?> clazz = Class.forName(packageName + '.' + className);
                            if (clazz.isAnnotationPresent(annotationClass.asSubclass(java.lang.annotation.Annotation.class))) {
                                classes.add(clazz);
                            }
                        } catch (ClassNotFoundException e) {
                            log.error("Exception caught!", e);
                        }
                    });
                }
            }
        } catch (IOException | URISyntaxException e) {
            log.error("Exception caught!", e);
        }
        return classes;
    }
}
