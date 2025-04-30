package org.pixel.blueprint;

import org.pixel.blueprint.annotation.Auto;
import org.pixel.blueprint.annotation.Blueprint;
import org.pixel.blueprint.annotation.Component;
import org.pixel.blueprint.annotation.Service;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public class BlueprintLoader {

    private static final Logger log = LoggerFactory.getLogger(BlueprintLoader.class);

    /**
     * Automatically search and load any Blueprint configuration in the given package name (including sub-packages).
     * This function uses the global component repository.
     *
     * @param packageNames The array of target base package names.
     */
    public static void load(String[] packageNames) {
        load(BlueprintRepository.getDefault(), packageNames);
    }

    /**
     * Automatically search and load any Blueprint configuration in the given package name (including sub-packages).
     *
     * @param repository   The component repository.
     * @param packageNames The array of target base package names.
     */
    public static void load(BlueprintRepository repository, String[] packageNames) {
        for (String packageName : packageNames) {
            load(repository, packageName);
        }
    }

    private static void load(BlueprintRepository repository, String packageName) {
        Map<String, Set<String>> dependencyGraph = new HashMap<>();
        Map<String, Method> componentMethods = new HashMap<>();

        Set<Class<?>> blueprintClasses = PackageScanner.findClassesWithAnnotation(packageName, Blueprint.class);
        Set<Class<?>> serviceClasses = PackageScanner.findClassesWithAnnotation(packageName, Service.class);

        // Step 1: Register components from Blueprint classes
        for (Class<?> blueprint : blueprintClasses) {
            for (var method : blueprint.getDeclaredMethods()) {
                if (!method.isAnnotationPresent(Component.class)) {
                    continue;
                }

                String componentName = getComponentName(method);
                if (componentName == null || componentName.isEmpty()) {
                    log.warn("Blueprint {0} has unnamed Component annotation!", blueprint.getSimpleName());
                    continue;
                }

                dependencyGraph.putIfAbsent(componentName, new HashSet<>());

                // Collect dependencies based on parameters annotated with @Auto
                for (Parameter parameter : method.getParameters()) {
                    if (parameter.isAnnotationPresent(Auto.class)) {
                        String dependencyName = getDependencyName(parameter);
                        if (dependencyName == null || dependencyName.isEmpty()) {
                            continue;
                        }

                        dependencyGraph.get(componentName).add(dependencyName);
                    }
                }
                componentMethods.put(componentName, method);
            }
        }

        // Step 2: Register services and add them to the dependency graph
        Map<String, Class<?>> serviceClassesMap = new HashMap<>();
        for (Class<?> serviceClass : serviceClasses) {
            String serviceName = getServiceName(serviceClass);
            if (serviceName == null || serviceName.isEmpty()) {
                continue;
            }



            serviceClassesMap.put(serviceName, serviceClass);
            dependencyGraph.putIfAbsent(serviceName, new HashSet<>());

            // Collect dependencies based on constructor parameters and fields annotated with @Auto
            Arrays.stream(serviceClass.getDeclaredConstructors())
                    .flatMap(constructor -> Arrays.stream(constructor.getParameters()))
                    .filter(parameter -> parameter.isAnnotationPresent(Auto.class))
                    .forEach(parameter -> {
                        String dependencyName = getDependencyName(parameter);
                        if (dependencyName == null) {
                            return;
                        }
                        dependencyGraph.get(serviceName).add(dependencyName);
                    });
        }

        // Step 3: Perform topological sort to determine load order
        List<String> sortedComponents = topologicalSort(dependencyGraph);

        // Step 4: Instantiate components and services in sorted order
        for (String componentName : sortedComponents) {
            if (componentMethods.containsKey(componentName)) {
                instantiateComponent(repository, componentMethods.get(componentName), componentName);
            } else if (serviceClassesMap.containsKey(componentName)) {
                instantiateService(repository, serviceClassesMap.get(componentName), componentName);
            }
        }
    }

    private static String getComponentName(Method method) {
        Component annotation = method.getAnnotation(Component.class);
        if (annotation == null) {
            return null;
        }

        return annotation.value().isEmpty() ? method.getName() : annotation.value();
    }

    private static String getServiceName(Class<?> serviceClass) {
        Service annotation = serviceClass.getAnnotation(Service.class);
        if (annotation == null) {
            return null;
        }

        return annotation.value().isEmpty() ? serviceClass.getSimpleName() : annotation.value();
    }

    private static String getDependencyName(Parameter parameter) {
        Auto annotation = parameter.getAnnotation(Auto.class);
        if (annotation == null) {
            return null;
        }

        return !annotation.value().isEmpty() ? annotation.value() : parameter.getName();
    }

    public static List<String> topologicalSort(Map<String, Set<String>> dependencyGraph) {
        List<String> sorted = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> resolved = new HashSet<>();

        for (String component : dependencyGraph.keySet()) {
            if (!resolved.contains(component)) {
                resolveDependencies(component, dependencyGraph, visited, resolved, sorted);
            }
        }
        return sorted;
    }

    private static void resolveDependencies(
            String component, Map<String, Set<String>> graph,
            Set<String> visited, Set<String> resolved, List<String> sorted) {

        if (visited.contains(component)) {
            throw new IllegalStateException("Circular dependency detected involving component: " + component);
        }

        if (!resolved.contains(component)) {
            visited.add(component);
            for (String dependency : graph.getOrDefault(component, Collections.emptySet())) {
                resolveDependencies(dependency, graph, visited, resolved, sorted);
            }
            visited.remove(component);
            resolved.add(component);
            sorted.add(component);
        }
    }

    private static void instantiateComponent(BlueprintRepository repository, Method method, String componentName) {
        try {
            Object blueprintInstance = method.getDeclaringClass().getDeclaredConstructor().newInstance();
            Object[] params = BlueprintUtil.resolveParameters(repository, method.getParameters());
            Object result = method.invoke(blueprintInstance, params);
            repository.registerComponent(result.getClass(), result, componentName);
        } catch (Exception e) {
            log.error("Failed to instantiate component: {0}.", componentName, e);
        }
    }

    private static void instantiateService(BlueprintRepository repository, Class<?> serviceClass, String serviceName) {
        try {
            Object serviceInstance = BlueprintAssembler.assemble(repository, serviceClass);
            repository.registerService(serviceClass, serviceInstance, serviceName);
        } catch (Exception e) {
            log.error("Failed to instantiate service: {0}.", serviceName, e);
        }
    }
}
