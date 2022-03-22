package org.pixel.ext.ecs.lifecycle;

public interface Attachable<T> {

    /**
     * Called when a new parent is attached.
     *
     * @param parent         The new parent.
     * @param previousParent The previous parent or null if there was none.
     */
    void attached(T parent, T previousParent);

    /**
     * Called when a parent is detached.
     *
     * @param previousParent The previous parent.
     */
    void detached(T previousParent);
}
