package org.pixel.ext.decs;

import org.pixel.commons.DeltaTime;
import org.pixel.commons.data.DataMap;
import org.pixel.commons.lifecycle.Disposable;
import org.pixel.commons.lifecycle.Drawable;
import org.pixel.commons.lifecycle.Loadable;
import org.pixel.commons.lifecycle.Updatable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The main container for entities, components, and systems, using a highly optimized Group pattern.
 */
public class GameWorld implements Loadable, Updatable, Drawable, Disposable {
    //region Fields

    public static final String DEFAULT_SCOPE = "__PIXEL_INTERNAL_DEFAULT_SCOPE__";

    private final Map<String, List<GameSystem>> systemsByScope = new HashMap<>();
    private final List<String> activeScopes = new ArrayList<>();
    /**
     * The main data store. Maps an entity's ID to a map of its components, keyed by component class.
     * E.g., { entityId -> { PositionComponent.class -> PositionComponent_instance, ... } }
     */
    private final Map<Integer, Map<Class<?>, GameComponent>> componentStore = new HashMap<>();
    /**
     * The primary cache for Group objects. Maps a set of component classes (the query) to a live Group.
     * This provides O(1) access to previously requested groups.
     */
    private final Map<Set<Class<?>>, GameGroup> groupCache = new HashMap<>();
    /**
     * A reverse index used for high-speed updates. Maps a single component class (including superclasses and
     * interfaces) to a list of all groups that are interested in that component. This avoids iterating all groups
     * when a component is changed.
     */
    private final Map<Class<?>, List<GameGroup>> componentToGroupIndex = new HashMap<>();
    /**
     * A cache for class hierarchy lookups to improve performance of polymorphic queries.
     */
    private final Map<Class<?>, Set<Class<?>>> typeHierarchyCache = new HashMap<>();
    /**
     * The next available entity ID.
     */
    private int nextEntityId = 0;
    /**
     * A shared data map for this world instance, allowing systems to share and access world-level context.
     */
    private final DataMap data = new DataMap();

    //endregion

    //region Constructor

    public GameWorld() {
        setScope(DEFAULT_SCOPE);
    }

    //endregion

    //region Lifecycle & System Management

    /**
     * Gets the shared DataMap for this world.
     *
     * @return The world's shared DataMap.
     */
    public DataMap getData() {
        return data;
    }

    /**
     * Gets a list of all systems that are currently active based on the active scopes.
     *
     * @return A new list containing all active systems.
     */
    public List<GameSystem> getSystems() {
        List<GameSystem> activeSystems = new ArrayList<>();
        for (String scopeName : activeScopes) {
            List<GameSystem> systems = systemsByScope.get(scopeName);
            if (systems != null) {
                activeSystems.addAll(systems);
            }
        }
        return activeSystems;
    }

    /**
     * Adds a system to the world, assigning it to the default scope.
     *
     * @param system The system to add.
     */
    public void addSystem(GameSystem system) {
        addSystem(system, DEFAULT_SCOPE);
    }

    /**
     * Adds a system to the world, assigning it to a specific scope.
     *
     * @param system    The system to add.
     * @param scopeName The name of the scope to assign the system to.
     */
    public void addSystem(GameSystem system, String scopeName) {
        systemsByScope.computeIfAbsent(scopeName, k -> new ArrayList<>()).add(system);
    }

    /**
     * Removes a system from the world from all scopes and calls its dispose method.
     *
     * @param system The system to remove.
     */
    public void removeSystem(GameSystem system) {
        if (system == null) {
            return;
        }
        boolean removed = false;
        for (List<GameSystem> systemList : systemsByScope.values()) {
            if (systemList.remove(system)) {
                removed = true; // Keep searching in case it was added to multiple scopes
            }
        }
        if (removed) {
            system.dispose();
        }
    }

    /**
     * Activates a scope, so its systems will be processed in the game loop.
     *
     * @param scopeName The name of the scope to activate.
     */
    public void activateScope(String scopeName) {
        if (!activeScopes.contains(scopeName)) {
            activeScopes.add(scopeName);
        }
    }

    /**
     * Deactivates a scope, so its systems will no longer be processed.
     *
     * @param scopeName The name of the scope to deactivate.
     */
    public void deactivateScope(String scopeName) {
        activeScopes.remove(scopeName);
    }

    /**
     * Deactivates all currently active scopes and activates the specified ones.
     * The default scope will remain active if it was active before, unless explicitly deactivated.
     *
     * @param scopeNames The names of the scopes to activate.
     */
    public void setScope(String... scopeNames) {
        boolean isDefaultActive = activeScopes.contains(DEFAULT_SCOPE);
        activeScopes.clear();
        if (isDefaultActive) {
            activeScopes.add(DEFAULT_SCOPE);
        }
        if (scopeNames != null) {
            for (String scopeName : scopeNames) {
                activateScope(scopeName);
            }
        }
    }

    @Override
    public void load() {
        for (List<GameSystem> systemList : systemsByScope.values()) {
            for (GameSystem system : systemList) {
                system.load();
            }
        }
    }

    @Override
    public void update(DeltaTime delta) {
        for (String scopeName : activeScopes) {
            List<GameSystem> systems = systemsByScope.get(scopeName);
            if (systems != null) {
                for (GameSystem system : systems) {
                    if (system.isEnabled()) {
                        system.update(delta);
                    }
                }
            }
        }
    }

    @Override
    public void draw(DeltaTime delta) {
        for (String scopeName : activeScopes) {
            List<GameSystem> systems = systemsByScope.get(scopeName);
            if (systems != null) {
                for (GameSystem system : systems) {
                    if (system.isEnabled()) {
                        system.draw(delta);
                    }
                }
            }
        }
    }

    @Override
    public void dispose() {
        for (List<GameSystem> systemList : systemsByScope.values()) {
            for (GameSystem system : systemList) {
                system.dispose();
            }
        }
        systemsByScope.clear();
        activeScopes.clear();
        componentStore.clear();
        groupCache.clear();
        componentToGroupIndex.clear();
        data.clear();
        typeHierarchyCache.clear();
        nextEntityId = 0;
    }

    //endregion

    //region Entity & Component Management

    /**
     * Creates a new entity.
     *
     * @return The new entity.
     */
    public GameEntity createEntity() {
        var entity = new GameEntity(nextEntityId++);
        componentStore.put(entity.id(), new HashMap<>());
        return entity;
    }

    /**
     * Destroys an entity and removes   all its components.
     *
     * @param entity The entity to destroy.
     */
    public void destroyEntity(GameEntity entity) {
        var components = componentStore.get(entity.id());
        if (components == null) return;

        // Gather a unique set of all groups this entity belongs to.
        Set<GameGroup> groupsToUpdate = new HashSet<>();
        for (Class<?> componentType : components.keySet()) {
            Set<Class<?>> allTypes = getAllTypes(componentType);
            if (allTypes != null) {
                for (Class<?> type : allTypes) {
                    List<GameGroup> affectedGroups = componentToGroupIndex.get(type);
                    if (affectedGroups != null) {
                        groupsToUpdate.addAll(affectedGroups);
                    }
                }
            }
        }

        // Remove the entity from each unique group just once.
        for (GameGroup group : groupsToUpdate) {
            group.remove(entity);
        }

        componentStore.remove(entity.id());
    }

    /**
     * Adds a component to an entity.
     *
     * @param entity    The entity.
     * @param component The component to add.
     */
    public void addComponent(GameEntity entity, GameComponent component) {
        var entityComponents = componentStore.get(entity.id());
        if (entityComponents == null) return; // Or throw exception

        Class<?> componentType = component.getClass();
        registerType(componentType); // Ensure type hierarchy is cached

        entityComponents.put(componentType, component);

        // Use the index to efficiently update only relevant groups for all parent types
        Set<Class<?>> allTypes = getAllTypes(componentType);
        if (allTypes == null) return;

        Set<GameGroup> processedGroups = new HashSet<>();
        for (Class<?> type : allTypes) {
            List<GameGroup> affectedGroups = componentToGroupIndex.get(type);
            if (affectedGroups != null) {
                for (GameGroup group : affectedGroups) {
                    // Process each affected group only once
                    if (processedGroups.add(group)) {
                        if (!group.contains(entity) && matches(entity, group.getComponentTypes())) {
                            group.add(entity);
                        }
                    }
                }
            }
        }
    }

    /**
     * Removes a component from an entity.
     *
     * @param entity        The entity.
     * @param componentType The type of the component to remove.
     */
    public void removeComponent(GameEntity entity, Class<?> componentType) {
        var entityComponents = componentStore.get(entity.id());
        if (entityComponents == null || !entityComponents.containsKey(componentType)) return;

        // Use the index to efficiently update only relevant groups for all parent types
        Set<Class<?>> allTypes = getAllTypes(componentType);
        if (allTypes == null) return;

        Set<GameGroup> processedGroups = new HashSet<>();
        for (Class<?> type : allTypes) {
            List<GameGroup> affectedGroups = componentToGroupIndex.get(type);
            if (affectedGroups != null) {
                for (GameGroup group : affectedGroups) {
                    // Process each affected group only once
                    if (processedGroups.add(group)) {
                        // If the entity was in the group, its match is now invalid because we are removing a component.
                        if (group.contains(entity)) {
                            group.remove(entity);
                        }
                    }
                }
            }
        }

        entityComponents.remove(componentType);
    }

    /**
     * Gets a component from an entity. Note: This performs an exact type match.
     *
     * @param entity        The entity.
     * @param componentType The component type.
     * @param <T>           The component type.
     * @return The component, or null if the entity does not have it.
     */
    @SuppressWarnings("unchecked")
    public <T extends GameComponent> T getComponent(GameEntity entity, Class<T> componentType) {
        var components = componentStore.get(entity.id());
        return components != null ? (T) components.get(componentType) : null;
    }

    /**
     * Checks if an entity has a specific component (including subclasses).
     *
     * @param entity        The entity.
     * @param componentType The component type.
     * @return True if the entity has the component, false otherwise.
     */
    public boolean hasComponent(GameEntity entity, Class<?> componentType) {
        var components = componentStore.get(entity.id());
        if (components == null) return false;

        for (Class<?> actualType : components.keySet()) {
            if (componentType.isAssignableFrom(actualType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets all components of a given entity.
     *
     * @param entity The entity.
     * @return A map of all components for the entity, or null if the entity doesn't exist.
     */
    public Map<Class<?>, GameComponent> getComponents(GameEntity entity) {
        return componentStore.get(entity.id());
    }

    //endregion

    //region Group Management

    /**
     * Gets a live, managed Group of entities that have the specified component types (including subclasses).
     *
     * @param componentTypes The component types to match.
     * @return A Group containing all entities that match the criteria.
     */
    public GameGroup getGroup(Class<?>... componentTypes) {
        var typeSet = new HashSet<>(Arrays.asList(componentTypes));
        if (groupCache.containsKey(typeSet)) {
            return groupCache.get(typeSet);
        }

        // Ensure all types in the query are registered and cached
        for (Class<?> type : typeSet) {
            registerType(type);
        }

        // Create a new group, populate it, and cache it
        GameGroup newGroup = new GameGroup(typeSet);
        for (Integer entityId : componentStore.keySet()) {
            if (matches(new GameEntity(entityId), typeSet)) {
                newGroup.add(new GameEntity(entityId));
            }
        }

        groupCache.put(typeSet, newGroup);

        // Update the reverse index for all types in the query
        for (Class<?> type : typeSet) {
            componentToGroupIndex.computeIfAbsent(type, k -> new ArrayList<>()).add(newGroup);
        }

        return newGroup;
    }

    private boolean matches(GameEntity entity, Set<Class<?>> requiredTypes) {
        var entityComponents = componentStore.get(entity.id());
        if (entityComponents == null) return false;

        for (Class<?> requiredType : requiredTypes) {
            boolean foundMatch = false;
            for (Class<?> actualType : entityComponents.keySet()) {
                if (requiredType.isAssignableFrom(actualType)) {
                    foundMatch = true;
                    break;
                }
            }
            if (!foundMatch) {
                return false; // This entity is missing a required component (or a subclass of it)
            }
        }
        return true;
    }

    private void registerType(Class<?> type) {
        if (typeHierarchyCache.containsKey(type) || type == null) {
            return;
        }

        Set<Class<?>> types = new HashSet<>();
        types.add(type);

        Class<?> superclass = type.getSuperclass();
        if (superclass != null) {
            registerType(superclass);
            types.addAll(getAllTypes(superclass));
        }

        for (Class<?> anInterface : type.getInterfaces()) {
            registerType(anInterface);
            types.addAll(getAllTypes(anInterface));
        }

        typeHierarchyCache.put(type, types);
    }

    private Set<Class<?>> getAllTypes(Class<?> type) {
        return typeHierarchyCache.get(type);
    }

    //endregion
}
