/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.tiled.content;

import org.pixel.commons.lifecycle.Disposable;

public class TileMap implements Disposable {
    private int width;
    private int height;

    public TileMap() {

    }

    @Override
    public void dispose() {

    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
