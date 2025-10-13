package org.pixel.ext.decs;

import lombok.Getter;

/**
 * A simple entity, which is just a wrapper for an ID.
 */
@Getter
public class Entity {
    /**
     * -- GETTER --
     *  Gets the entity's ID.
     *
     * @return The entity's ID.
     */
    private final int id;

    /**
     * Constructor.
     *
     * @param id The entity's ID.
     */
    public Entity(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return id == entity.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
