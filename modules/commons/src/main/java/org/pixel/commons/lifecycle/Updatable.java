/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.commons.lifecycle;

import org.pixel.commons.DeltaTime;

public interface Updatable {

    /**
     * Update function
     *
     * @param delta Delta time
     */
    void update(DeltaTime delta);
}
