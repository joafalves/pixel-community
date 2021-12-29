package org.pixel.ext.ecs.lifecycle;

import org.pixel.ext.ecs.GameObject;

public interface Attachable {

    /**
     * Called when a new parent is attached.
     *
     * @param parent         The new parent.
     * @param previousParent The previous parent or null if there was none.
     */
    void attached(GameObject parent, GameObject previousParent);

    /**
     * Called when a parent is detached.
     *
     * @param previousParent The previous parent.
     */
    void detached(GameObject previousParent);
}
