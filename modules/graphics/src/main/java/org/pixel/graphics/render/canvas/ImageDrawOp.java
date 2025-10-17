/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.render.canvas;

import org.pixel.commons.Color;
import org.pixel.content.Texture;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;

/**
 * Fluent builder for drawing images/textures on a Canvas.
 * 
 * <p>Example usage:
 * <pre>
 * canvas.image(texture, 100, 100, 200, 150)
 *     .withTint(Color.RED)
 *     .withAlpha(0.5f);
 * </pre>
 */
public class ImageDrawOp extends DrawOp<ImageDrawOp> {

    private Texture texture;
    private float x, y, width, height;
    private Color tint = Color.WHITE;
    private Rectangle source = null; // null = use full texture
    private float rotation = 0;
    private Vector2 anchor = new Vector2(0, 0);

    /**
     * Package-private constructor.
     * Instances are created and pooled by Canvas.
     */
    ImageDrawOp(CanvasRenderer renderer) {
        super(renderer);
    }

    @Override
    protected ImageDrawOp reset() {
        super.reset();
        this.texture = null;
        this.x = 0;
        this.y = 0;
        this.width = 0;
        this.height = 0;
        this.tint = Color.WHITE;
        this.source = null;
        this.rotation = 0;
        this.anchor.set(0, 0);
        return this;
    }

    /**
     * Internal method to set the image data.
     * Called by Canvas.image() to initialize the operation.
     */
    ImageDrawOp setImage(Texture texture, float x, float y, float width, float height) {
        reset();
        this.texture = texture;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        return this;
    }

    /**
     * Set a tint color for the image.
     * Color.WHITE (default) means no tinting.
     * 
     * @param tint The tint color
     * @return This instance for method chaining
     */
    public ImageDrawOp withTint(Color tint) {
        this.tint = tint;
        return this;
    }

    /**
     * Set the alpha (opacity) of the image.
     * 
     * @param alpha Alpha value (0.0 = transparent, 1.0 = opaque)
     * @return This instance for method chaining
     */
    public ImageDrawOp withAlpha(float alpha) {
        this.tint = new Color(tint.getRed(), tint.getGreen(), tint.getBlue(), alpha);
        return this;
    }

    /**
     * Set the source rectangle to draw a portion of the texture.
     * 
     * @param source The source rectangle in pixel coordinates
     * @return This instance for method chaining
     */
    public ImageDrawOp withSource(Rectangle source) {
        this.source = source;
        return this;
    }

    /**
     * Set the source rectangle to draw a portion of the texture.
     * 
     * @param srcX      Source X in pixels
     * @param srcY      Source Y in pixels
     * @param srcWidth  Source width in pixels
     * @param srcHeight Source height in pixels
     * @return This instance for method chaining
     */
    public ImageDrawOp withSource(float srcX, float srcY, float srcWidth, float srcHeight) {
        this.source = new Rectangle(srcX, srcY, srcWidth, srcHeight);
        return this;
    }

    /**
     * Set rotation angle in radians around the top-left corner (0, 0).
     * 
     * @param rotation Rotation angle in radians
     * @return This instance for method chaining
     */
    public ImageDrawOp withRotation(float rotation) {
        this.rotation = rotation;
        this.anchor.set(0, 0);
        return this;
    }

    /**
     * Set rotation angle in radians with a custom anchor point.
     * The anchor determines the point around which the image rotates.
     * 
     * @param rotation Rotation angle in radians
     * @param anchorX  Anchor X (0 = left, 0.5 = center, 1 = right)
     * @param anchorY  Anchor Y (0 = top, 0.5 = center, 1 = bottom)
     * @return This instance for method chaining
     */
    public ImageDrawOp withRotation(float rotation, float anchorX, float anchorY) {
        this.rotation = rotation;
        this.anchor.set(anchorX, anchorY);
        return this;
    }

    @Override
    protected boolean isReadyToExecute() {
        return texture != null;
    }

    @Override
    protected void performDraw() {
        canvas.drawImage(texture, source, new Rectangle(x, y, width, height), 
                          rotation, anchor, tint);
    }
}
