/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.physics.handler;

import pixel.physics.CollisionGroup;

public interface CollisionHandler {

    /**
     * Determines if this collision handler can process the given group
     *
     * @param group
     * @return
     */
    boolean canHandle(CollisionGroup group);

    /**
     * Process the collision of the given group
     *
     * @param group
     */
    void handleCollision(CollisionGroup group);

}
