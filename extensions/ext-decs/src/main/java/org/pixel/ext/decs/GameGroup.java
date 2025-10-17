package org.pixel.ext.decs;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A live, automatically-updated collection of entities that match a specific set of components.
 */
public class GameGroup implements Iterable<GameEntity> {

    private final Set<Class<?>> componentTypes;
    private final Set<GameEntity> entities;

    /**
     * Constructor.
     *
     * @param componentTypes The set of component types that define this group.
     */
    GameGroup(Set<Class<?>> componentTypes) {
        this.componentTypes = componentTypes;
        this.entities = new LinkedHashSet<>(); // Use LinkedHashSet for O(1) ops and ordered iteration
    }

    /**
     * Gets the collection of entities in this group.
     *
     * @return The collection of entities.
     */
    public Collection<GameEntity> getEntities() {
        return entities;
    }

    /**
     * Gets the set of component types that define this group.
     *
     * @return The set of component types.
     */
    Set<Class<?>> getComponentTypes() {
        return componentTypes;
    }

    /**
     * Checks if the group contains the given entity.
     *
     * @param entity The entity to check.
     * @return True if the group contains the entity, false otherwise.
     */
    boolean contains(GameEntity entity) {
        return entities.contains(entity);
    }

    /**
     * Adds an entity to the group.
     *
     * @param entity The entity to add.
     */
    void add(GameEntity entity) {
        entities.add(entity);
    }

    /**
     * Removes an entity from the group.
     *
     * @param entity The entity to remove.
     */
    void remove(GameEntity entity) {
        entities.remove(entity);
    }

    @Override
    public Iterator<GameEntity> iterator() {
        return entities.iterator();
    }
}
