package org.pixel.blueprint;

import org.pixel.blueprint.annotation.Auto;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;

public class ComponentAssembler {

    /**
     * Creates and assembles a new instance of a class.
     * This function uses the global component repository.
     *
     * @param clazz The class type.
     * @return The assembled instance.
     */
    public static <T> T assemble(Class<T> clazz) {
        return assemble(BlueprintContext.globalRepository, clazz);
    }

    /**
     * Creates and assembles a new instance of a class.
     *
     * @param repository The component repository.
     * @param clazz The class type.
     * @return The assembled instance.
     */
    public static <T> T assemble(ComponentRepository repository, Class<T> clazz) {
        try {
            // Find the most suitable constructor (we'll pick the one with the most parameters, if any)
            Constructor<?>[] constructors = clazz.getConstructors();
            Constructor<?> constructor = Arrays.stream(constructors)
                    .max(Comparator.comparingInt(Constructor::getParameterCount))
                    .orElseThrow(() -> new IllegalArgumentException("No public constructors available for " + clazz.getName()));

            // Resolve constructor parameters
            Object[] params = BlueprintUtil.resolveParameters(repository, constructor.getParameters());

            // Instantiate the class with resolved parameters
            @SuppressWarnings("unchecked")
            T instance = (T) constructor.newInstance(params);

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
        return assemble(BlueprintContext.globalRepository, instance);
    }

    /**
     * Automatically injects fields annotated with `@Auto` in the provided instance.
     * Returns true if all @Auto fields are successfully injected, or if there are no @Auto fields.
     *
     * @param repository The component repository.
     * @param instance The instance to bootstrap.
     * @return true if all @Auto fields are assigned, false if any field remains unassigned.
     */
    public static boolean assemble(ComponentRepository repository, Object instance) {
        Field[] fields = instance.getClass().getDeclaredFields();
        boolean allInjected = true;

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

        return allInjected;
    }
}
