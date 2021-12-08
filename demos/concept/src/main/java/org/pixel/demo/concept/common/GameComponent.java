/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.demo.concept.common;

import org.pixel.commons.lifecycle.Updatable;

public abstract class GameComponent implements Updatable {

    protected GameObject parent;

    public void setParent(GameObject parent) {
        this.parent = parent;
    }
}
