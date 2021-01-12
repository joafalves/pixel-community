/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.content;

import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;

/**
 * @author João Filipe Alves
 */
public class TextureFrame {

    private Rectangle source;
    private Vector2 pivot;

    public TextureFrame(Rectangle source, Vector2 pivot) {
        this.source = source;
        this.pivot = pivot;
    }

    public Vector2 getPivot() {
        return pivot;
    }

    public void setPivot(Vector2 pivot) {
        this.pivot = pivot;
    }

    public Rectangle getSource() {
        return source;
    }

    public void setSource(Rectangle source) {
        this.source = source;
    }
}
