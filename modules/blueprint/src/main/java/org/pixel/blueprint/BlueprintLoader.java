package org.pixel.blueprint;

import org.pixel.blueprint.annotation.Auto;
import org.pixel.blueprint.annotation.Blueprint;
import org.pixel.blueprint.annotation.Component;
import org.pixel.blueprint.annotation.Condition;
import org.pixel.blueprint.annotation.Conditional;
import org.pixel.blueprint.annotation.Scope;
import org.pixel.blueprint.annotation.ScopeType;
import org.pixel.blueprint.annotation.Service;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
        Map<String, Boolean> componentIsPrototype = new HashMap<>();
        Map<String, String[]> componentTags = new HashMap<>();

        Map<String, Class<?>> serviceClassesMap = new HashMap<>();
        Map<String, Boolean> serviceIsPrototype = new HashMap<>();
        Map<String, String[]> serviceTags = new HashMap<>();

        Set<Class<?>> blueprintClasses = PackageScanner.findClassesWithAnnotation(packageName, Blueprint.class);
        Set<Class<?>> serviceClasses = PackageScanner.findClassesWithAnnotation(packageName, Service.class);

        // Step 1: Register components from Blueprint classes
        for (Class<?> blueprint : blueprintClasses) {
            for (var method : blueprint.getDeclaredMethods()) {
                if (!method.isAnnotationPresent(Component.class)) {
                    continue;
                }

                // Evaluate conditional annotations
                if (!evaluateConditionals(method)) {
                    log.debug("Skipping component method '{0}' in blueprint '{1}' due to conditional evaluation.", method.getName(), blueprint.getSimpleName());
                    continue;
                }

                String componentName = getComponentName(method);
                if (componentName == null || componentName.isEmpty()) {
                    log.warn("Blueprint {0} has unnamed Component annotation!", blueprint.getSimpleName());
                    continue;
                }

                Component componentAnnotation = method.getAnnotation(Component.class);
                ScopeType scope = getComponentScope(method);

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
                componentIsPrototype.put(componentName, scope == ScopeType.PROTOTYPE);
                componentTags.put(componentName, componentAnnotation.tags());
            }
        }

        // Step 2: Register services and add them to the dependency graph
        for (Class<?> serviceClass : serviceClasses) {
            // Evaluate conditional annotations
            if (!evaluateConditionals(serviceClass)) {
                log.debug("Skipping service class '{0}' due to conditional evaluation.", serviceClass.getSimpleName());
                continue;
            }

            String serviceName = getServiceName(serviceClass);
            if (serviceName == null || serviceName.isEmpty()) {
                continue;
            }

            Service serviceAnnotation = serviceClass.getAnnotation(Service.class);
            ScopeType scope = getServiceScope(serviceClass);

            serviceClassesMap.put(serviceName, serviceClass);
            serviceIsPrototype.put(serviceName, scope == ScopeType.PROTOTYPE);
            serviceTags.put(serviceName, serviceAnnotation.tags());
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
                if (Boolean.TRUE.equals(componentIsPrototype.get(componentName))) {
                    // Store prototype factory, don't instantiate yet
                    Method method = componentMethods.get(componentName);
                    repository.registerPrototypeComponent(componentName, method, componentTags.get(componentName));
                } else {
                    instantiateComponent(repository, componentMethods.get(componentName), componentName, componentTags.get(componentName));
                }
            } else if (serviceClassesMap.containsKey(componentName)) {
                if (Boolean.TRUE.equals(serviceIsPrototype.get(componentName))) {
                    // Store prototype factory, don't instantiate yet
                    Class<?> clazz = serviceClassesMap.get(componentName);
                    repository.registerPrototypeService(componentName, clazz, serviceTags.get(componentName));
                } else {
                    instantiateService(repository, serviceClassesMap.get(componentName), componentName, serviceTags.get(componentName));
                }
            }
        }
    }

    /**
     * Evaluate @Conditional annotations on a method.
     *
     * @param method The method to evaluate.
     * @return true if all conditions match or no conditions are present.
     */
    private static boolean evaluateConditionals(Method method) {
        if (!method.isAnnotationPresent(Conditional.class)) {
            return true;
        }
        Conditional conditional = method.getAnnotation(Conditional.class);
        return evaluateConditions(conditional.value());
    }

    /**
     * Evaluate @Conditional annotations on a class.
     *
     * @param clazz The class to evaluate.
     * @return true if all conditions match or no conditions are present.
     */
    private static boolean evaluateConditionals(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Conditional.class)) {
            return true;
        }
        Conditional conditional = clazz.getAnnotation(Conditional.class);
        return evaluateConditions(conditional.value());
    }

    private static boolean evaluateConditions(Class<? extends Condition>[] conditionClasses) {
        for (Class<? extends Condition> conditionClass : conditionClasses) {
            try {
                Condition condition = conditionClass.getDeclaredConstructor().newInstance();
                if (!condition.matches()) {
                    return false;
                }
            } catch (Exception e) {
                throw new BlueprintException("Failed to evaluate condition: " + conditionClass.getName(), e);
            }
        }
        return true;
    }

    private static ScopeType getComponentScope(Method method) {
        if (method.isAnnotationPresent(Scope.class)) {
            return method.getAnnotation(Scope.class).value();
        }
        return ScopeType.SINGLETON;
    }

    private static ScopeType getServiceScope(Class<?> serviceClass) {
        if (serviceClass.isAnnotationPresent(Scope.class)) {
            return serviceClass.getAnnotation(Scope.class).value();
        }
        return ScopeType.SINGLETON;
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
            throw new BlueprintException("Circular dependency detected involving component: " + component);
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

    private static void instantiateComponent(BlueprintRepository repository, Method method, String componentName, String[] tags) {
        try {
            Object blueprintInstance = method.getDeclaringClass().getDeclaredConstructor().newInstance();
            Object[] params = BlueprintUtil.resolveParameters(repository, method.getParameters());
            Object result = method.invoke(blueprintInstance, params);
            repository.registerComponent(result.getClass(), result, componentName, tags);
        } catch (Exception e) {
            throw new BlueprintException("Failed to instantiate component: " + componentName, e);
        }
    }

    private static void instantiateService(BlueprintRepository repository, Class<?> serviceClass, String serviceName, String[] tags) {
        try {
            Object serviceInstance = BlueprintAssembler.assemble(repository, serviceClass);
            repository.registerService(serviceClass, serviceInstance, serviceName, tags);
        } catch (Exception e) {
            throw new BlueprintException("Failed to instantiate service: " + serviceName, e);
        }
    }
}
