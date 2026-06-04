package org.pixel.blueprint;

import org.pixel.blueprint.annotation.Auto;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

public class BlueprintUtil {
    public static Object[] resolveParameters(BlueprintRepository repository, Parameter[] parameters) {
        if (parameters == null || parameters.length == 0) {
            return new Object[0];
        }

        return Arrays.stream(parameters)
                .map(parameter -> resolveParameter(repository, parameter))
                .toArray();
    }

    private static Object resolveParameter(BlueprintRepository repository, Parameter parameter) {
        Auto autoAnnotation = parameter.getAnnotation(Auto.class);
        boolean isAuto = autoAnnotation != null;
        String name = (isAuto && !autoAnnotation.value().isEmpty())
                ? autoAnnotation.value()  // Use specified value in @Auto if present
                : parameter.getName();      // Otherwise, fallback to the parameter name

        Class<?> type = parameter.getType();

        // Handle tag-based injection for parameters
        if (isAuto && autoAnnotation.tags().length > 0) {
            // For parameters, we only support single instance injection by tag (first match)
            var result = repository.getByTags(type, autoAnnotation.tags());
            if (!result.isEmpty()) {
                return result.get(0);
            }
            throw new BlueprintException("No instance found with tags " + Arrays.toString(autoAnnotation.tags()) + " for parameter: " + parameter.getName());
        }

        // Try to find an instance by the specified name from @Auto or parameter name
        if (repository.getNamedComponents().containsKey(name)) {
            return repository.getNamedComponents().get(name);
        }

        if (repository.getNamedServices().containsKey(name)) {
            return repository.getNamedServices().get(name);
        }

        // Check for prototype factories
        if (repository.getPrototypeComponentFactory(name).isPresent()) {
            Method method = repository.getPrototypeComponentFactory(name).get();
            try {
                Object blueprintInstance = method.getDeclaringClass().getDeclaredConstructor().newInstance();
                Object[] params = resolveParameters(repository, method.getParameters());
                return method.invoke(blueprintInstance, params);
            } catch (Exception e) {
                throw new BlueprintException("Failed to create prototype component: " + name, e);
            }
        }

        if (repository.getPrototypeServiceFactory(name).isPresent()) {
            Class<?> clazz = repository.getPrototypeServiceFactory(name).get();
            return BlueprintAssembler.assemble(repository, clazz);
        }

        // Fallback to looking for an instance by type (supports interface binding)
        Object byType = repository.uget(type);
        if (byType != null) {
            return byType;
        }

        // Search by parameter name as fallback for type-based lookup
        Object namedByParam = repository.uget(type, parameter.getName());
        if (namedByParam != null) {
            return namedByParam;
        }

        if (isAuto) {
            throw new BlueprintException("Dependency not found for parameter '" + parameter.getName() + "' of type " + type.getName());
        }

        return null;
    }
}
