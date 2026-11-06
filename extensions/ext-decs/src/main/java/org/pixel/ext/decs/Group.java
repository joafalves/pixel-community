package org.pixel.ext.decs;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A live, automatically-updated collection of entities that match a specific set of components.
 */
public class Group implements Iterable<Entity> {

    private final Set<Class<?>> componentTypes;
    private final Set<Entity> entities;
    private Entity[] snapshot = new Entity[0];
    private boolean snapshotDirty;

    /**
     * Constructor.
     *
     * @param componentTypes The set of component types that define this group.
     */
    Group(Set<Class<?>> componentTypes) {
        this.componentTypes = componentTypes;
        this.entities = new LinkedHashSet<>(); // Use LinkedHashSet for O(1) ops and ordered iteration
    }

    /**
     * Gets the collection of entities in this group.
     *
     * @return The collection of entities.
     */
    public Collection<Entity> getEntities() {
        return java.util.Collections.unmodifiableSet(entities);
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
    boolean contains(Entity entity) {
        return entities.contains(entity);
    }

    /**
     * Adds an entity to the group.
     *
     * @param entity The entity to add.
     */
    void add(Entity entity) {
        if (entities.add(entity)) {
            snapshotDirty = true;
        }
    }

    /**
     * Removes an entity from the group.
     *
     * @param entity The entity to remove.
     */
    void remove(Entity entity) {
        if (entities.remove(entity)) {
            snapshotDirty = true;
        }
    }

    /**
     * Iterates a snapshot of the group, so systems may create/destroy entities or add/remove components while
     * iterating (previously a ConcurrentModificationException). Entities removed from the group during the
     * iteration are skipped; entities added during the iteration are visited on the next one.
     */
    @Override
    public Iterator<Entity> iterator() {
        if (snapshotDirty) {
            snapshot = entities.toArray(new Entity[0]);
            snapshotDirty = false;
        }
        final Entity[] current = snapshot;
        return new Iterator<>() {
            private int index;

            @Override
            public boolean hasNext() {
                // skip lazily, so removals done by the loop body are honoured
                while (index < current.length && !entities.contains(current[index])) {
                    index++;
                }
                return index < current.length;
            }

            @Override
            public Entity next() {
                if (!hasNext()) {
                    throw new java.util.NoSuchElementException();
                }
                return current[index++];
            }
        };
    }
}
