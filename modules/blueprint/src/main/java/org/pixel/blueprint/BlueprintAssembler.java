package org.pixel.blueprint;

import org.pixel.blueprint.annotation.Auto;
import org.pixel.blueprint.annotation.AfterAssembly;
import org.pixel.blueprint.annotation.Scheduled;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class BlueprintAssembler {

    private static final Logger log = LoggerFactory.getLogger(BlueprintAssembler.class);
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(
            Integer.parseInt(System.getenv().getOrDefault("PIXEL_BLUEPRINT_SCHEDULER_POOL_SIZE", "10"))
    );

    /**
     * Creates and assembles a new instance of a class.
     * This function uses the global component repository.
     *
     * @param clazz The class type.
     * @return The assembled instance.
     */
    public static <T> T assemble(Class<T> clazz) {
        return assemble(BlueprintRepository.getDefault(), clazz);
    }

    /**
     * Creates and assembles a new instance of a class.
     *
     * @param repository The component repository.
     * @param clazz      The class type.
     * @return The assembled instance.
     */
    public static <T> T assemble(BlueprintRepository repository, Class<T> clazz) {
        try {
            // Find the most suitable constructor (we'll pick the one with the most parameters, if any)
            Constructor<?>[] constructors = clazz.getConstructors();
            Constructor<?> constructor = Arrays.stream(constructors).max(Comparator.comparingInt(Constructor::getParameterCount)).orElseThrow(() -> new IllegalArgumentException("No public constructors available for " + clazz.getName()));

            // Resolve constructor parameters
            Object[] params = BlueprintUtil.resolveParameters(repository, constructor.getParameters());

            // Instantiate the class with resolved parameters
            @SuppressWarnings("unchecked") T instance = (T) constructor.newInstance(params);

            // Assemble any fields
            assemble(repository, instance);

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create and assemble instance for " + clazz.getName(), e);
        }
    }

    /**
     * Automatically injects fields annotated with `@Auto` in the provided instance.
     * Returns true if all @Auto fields are successfully injected, or if there are no @Auto fields.
     * This function uses the global component repository.
     *
     * @param instance The instance to bootstrap.
     * @return true if all @Auto fields are assigned, false if any field remains unassigned.
     */
    public static boolean assemble(Object instance) {
        return assemble(BlueprintRepository.getDefault(), instance);
    }

    /**
     * Automatically injects fields annotated with `@Auto` in the provided instance.
     * Returns true if all @Auto fields are successfully injected, or if there are no @Auto fields.
     *
     * @param repository The component repository.
     * @param instance   The instance to bootstrap.
     * @return true if all @Auto fields are assigned, false if any field remains unassigned.
     */
    public static boolean assemble(BlueprintRepository repository, Object instance) {
        Field[] fields = instance.getClass().getDeclaredFields();
        boolean allInjected = true;

        // Assemble fields
        for (Field field : fields) {
            if (field.isAnnotationPresent(Auto.class)) {
                Auto autoAnnotation = field.getAnnotation(Auto.class);
                String requiredName = autoAnnotation.value();
                field.setAccessible(true);

                try {
                    if (field.get(instance) == null) { // Only inject if field is null
                        Object dependency;

                        if (!requiredName.isEmpty()) {
                            // Get by name if a name is specified
                            dependency = repository.uget(field.getType(), requiredName);
                        } else {
                            // Does the field name match any named instance map?
                            dependency = repository.uget(field.getType(), field.getName());

                            if (dependency == null) {
                                // Otherwise, attempt to get by type
                                dependency = repository.uget(field.getType());
                            }
                        }

                        if (dependency != null) {
                            field.set(instance, dependency);
                        } else {
                            allInjected = false; // Mark as false if the dependency is missing
                        }
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to inject instance for field: " + field.getName(), e);
                }
            }
        }

        // Assemble method annotations
        assembleMethodAnnotations(repository, instance);

        return allInjected;
    }

    private static void assembleMethodAnnotations(BlueprintRepository repository, Object instance) {
        Method[] methods = instance.getClass().getDeclaredMethods();
        for (Method method : methods) {
            // Scheduled annotation:
            if (method.isAnnotationPresent(Scheduled.class)) {
                Scheduled scheduled = method.getAnnotation(Scheduled.class);
                long interval = scheduled.intervalMs();

                scheduler.scheduleAtFixedRate(() -> {
                    try {
                        method.setAccessible(true); // Ensure the method can be accessed
                        method.invoke(instance);
                    } catch (Exception e) {
                        log.error("Exception caught!", e);
                    }
                }, 0, interval, TimeUnit.MILLISECONDS);
            }

            // PostAssembly annotation:
            if (method.isAnnotationPresent(AfterAssembly.class)) {
                method.setAccessible(true);
                try {
                    // if async is true, execute the method in a separate thread:
                    if (method.getAnnotation(AfterAssembly.class).async()) {
                        scheduler.execute(() -> {
                            try {
                                method.invoke(instance);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                log.error("Exception caught!", e);
                            }
                        });
                    } else {
                        method.invoke(instance,
                                BlueprintUtil.resolveParameters(repository, method.getParameters())
                        );
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    log.error("Exception caught!", e);
                }
            }
        }
    }
}
