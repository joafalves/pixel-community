package org.pixel.ext.decs;

/**
 * A simple entity, which is just a wrapper for an ID.
 *
 * @param id -- GETTER --
 *           Gets the entity's ID.
 */
public record GameEntity(int id) {
    /**
     * Constructor.
     *
     * @param id The entity's ID.
     */
    public GameEntity {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameEntity entity = (GameEntity) o;
        return id == entity.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
