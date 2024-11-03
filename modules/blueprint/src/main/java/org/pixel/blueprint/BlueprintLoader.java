package org.pixel.blueprint;

import org.pixel.blueprint.annotation.Blueprint;
import org.pixel.blueprint.annotation.Component;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;

import java.util.Set;

public class BlueprintLoader {

    private static final Logger log = LoggerFactory.getLogger(BlueprintLoader.class);

    /**
     * Automatically search and load any Blueprint configuration in the given package name (including sub-packages).
     * This function uses the global component repository.
     *
     * @param packageNames The array of target base package names.
     */
    public static void load(String[] packageNames) {
        load(BlueprintContext.globalRepository, packageNames);
    }

    /**
     * Automatically search and load any Blueprint configuration in the given package name (including sub-packages).
     *
     * @param repository The component repository.
     * @param packageNames The array of target base package names.
     */
    public static void load(ComponentRepository repository, String[] packageNames) {
        for (String packageName : packageNames) {
            load(repository, packageName);
        }
    }

    private static void load(ComponentRepository repository, String packageName) {
        // This method should implement a package scanning logic
        // For simplicity, let's assume we have a method to find all classes in a package
        Set<Class<?>> blueprintClasses = PackageScanner.findClassesWithAnnotation(packageName, Blueprint.class);
        for (Class<?> blueprint : blueprintClasses) {
            for (var method : blueprint.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Component.class)) {
                    try {
                        // Create an instance of the schema class
                        Object instance = blueprint.getDeclaredConstructor().newInstance();

                        // Resolve method parameters
                        Object[] params = BlueprintUtil.resolveParameters(repository, method.getParameters());

                        // Invoke the method to get the instance
                        Object result = method.invoke(instance, params);

                        // Determine the name to register with
                        String name = method.getAnnotation(Component.class).value().isEmpty() ? method.getName() : method.getAnnotation(Component.class).value();

                        // Register the instance in the named instances pool to mitigate conflicts
                        repository.getNamedComponents().put(name, result);
                    } catch (Exception e) {
                        log.error("Exception caught!", e);
                    }
                }
            }
        }
    }

}
