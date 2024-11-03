package org.pixel.blueprint;

import org.pixel.blueprint.annotation.Auto;

import java.lang.reflect.Parameter;
import java.util.Arrays;

public class BlueprintUtil {
    public static Object[] resolveParameters(ComponentRepository repository, Parameter[] parameters) {
        if (parameters == null || parameters.length == 0) {
            return new Object[0];
        }

        return Arrays.stream(parameters)
                .map(parameter -> {
                    // Check if the parameter has the @Wired annotation with a specific value
                    Auto wiredAnnotation = parameter.getAnnotation(Auto.class);
                    String name = (wiredAnnotation != null && !wiredAnnotation.value().isEmpty())
                            ? wiredAnnotation.value()  // Use specified value in @Wired if present
                            : parameter.getName();      // Otherwise, fallback to the parameter name

                    Class<?> type = parameter.getType();

                    // Try to find an instance by the specified name from @Wired or parameter name
                    if (repository.getNamedComponents().containsKey(name)) {
                        return repository.getNamedComponents().get(name);
                    }

                    // Fallback to looking for an instance by type if no named instance found
                    return repository.getComponents().get(type);
                })
                .toArray();
    }
}
