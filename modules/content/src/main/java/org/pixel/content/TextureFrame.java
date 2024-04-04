/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.content;

import org.pixel.commons.AttributeMap;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;

public class TextureFrame {

    private Texture texture;
    private Rectangle source;
    private Vector2 pivot;
    private AttributeMap attributes;

    /**
     * Constructor.
     *
     * @param texture The texture this frame belongs to.
     * @param source  The source rectangle.
     * @param pivot   The pivot point.
     */
    public TextureFrame(Texture texture, Rectangle source, Vector2 pivot) {
        this.texture = texture;
        this.source = source;
        this.pivot = pivot;
    }

    /**
     * Constructor
     *
     * @param texture    The texture this frame belongs to.
     * @param source     The source rectangle.
     * @param pivot      The pivot point.
     * @param attributes The attributes (usually the meta associated to the frame).
     */
    public TextureFrame(Texture texture, Rectangle source, Vector2 pivot, AttributeMap attributes) {
        this.texture = texture;
        this.source = source;
        this.pivot = pivot;
        this.attributes = attributes;
    }

    /**
     * Get the pivot point.
     *
     * @return The pivot point.
     */
    public Vector2 getPivot() {
        return pivot;
    }

    /**
     * Set the pivot point.
     *
     * @param pivot The pivot point.
     */
    public void setPivot(Vector2 pivot) {
        this.pivot = pivot;
    }

    /**
     * Get the source rectangle.
     *
     * @return The source rectangle.
     */
    public Rectangle getSource() {
        return source;
    }

    /**
     * Set the source rectangle.
     *
     * @param source The source rectangle.
     */
    public void setSource(Rectangle source) {
        this.source = source;
    }

    /**
     * Get the attributes.
     *
     * @return The attributes.
     */
    public AttributeMap getAttributes() {
        return attributes;
    }

    /**
     * Set the attributes.
     *
     * @param attributes The attributes.
     */
    public void setAttributes(AttributeMap attributes) {
        this.attributes = attributes;
    }

    /**
     * Get the texture associated to this frame.
     *
     * @return The texture.
     */
    public Texture getTexture() {
        return texture;
    }

    /**
     * Set the texture associated to this frame.
     *
     * @param texture The texture.
     */
    public void setTexture(Texture texture) {
        this.texture = texture;
    }
}
