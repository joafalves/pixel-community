package org.pixel.ext.ecs;

import org.pixel.commons.DeltaTime;
import org.pixel.content.Texture;
import org.pixel.content.TextureFrame;
import org.pixel.graphics.Color;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Boundary;
import org.pixel.math.MathHelper;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;

/**
 * Positional sprite game object.
 */
public class Sprite extends GameObject {

    private transient Texture texture;
    private Rectangle textureSource;
    private Color overlayColor;
    private int sortingDepth;
    private final Vector2 pivot;

    /**
     * Constructor.
     *
     * @param name    The name of the Sprite.
     * @param texture The Texture of the Sprite.
     */
    public Sprite(String name, Texture texture) {
        this(name, texture, new Rectangle(0, 0, texture.getWidth(), texture.getHeight()));
    }

    /**
     * Constructor.
     *
     * @param name          The name of the Sprite.
     * @param texture       The Texture of the Sprite.
     * @param textureSource The source of the Texture.
     */
    public Sprite(String name, Texture texture, Rectangle textureSource) {
        super(name);
        if (texture == null) {
            throw new IllegalArgumentException("Texture cannot be null.");
        }

        this.texture = texture;
        this.textureSource = textureSource;
        this.pivot = Vector2.half();
        this.overlayColor = new Color(Color.WHITE);
        this.sortingDepth = 0;
    }

    /**
     * Constructor.
     *
     * @param name  The name of the Sprite.
     * @param frame The TextureFrame to extract information from.
     */
    public Sprite(String name, TextureFrame frame) {
        super(name);
        if (frame.getTexture() == null) {
            throw new IllegalArgumentException("Texture cannot be null.");
        }

        this.texture = frame.getTexture();
        this.textureSource = new Rectangle(frame.getSource());
        this.pivot = new Vector2(frame.getPivot());
        this.overlayColor = new Color(Color.WHITE);
        this.sortingDepth = 0;
    }

    @Override
    public void draw(DeltaTime delta, SpriteBatch spriteBatch) {
        super.draw(delta, spriteBatch);

        var transform = getTransform();
        spriteBatch.draw(texture, transform.getWorldPosition(), textureSource, overlayColor, pivot,
                transform.getWorldScaleX(), transform.getWorldScaleY(), transform.getWorldRotation(), sortingDepth);
    }

    @Override
    public GameObject copy() {
        var copy = (Sprite) super.copy();
        copy.texture = this.texture;
        return copy;
    }

    /**
     * Get the bounding box (rectangle) of the Sprite. This is similar to {@link #getBoundingBox()} but it doesn't
     * consider rotation.
     *
     * @return The bounding box (rectangle) of the Sprite.
     */
    public Rectangle getBoundingRect() {
        var transform = getTransform();
        var position = transform.getWorldPosition();
        var scaleX = MathHelper.abs(transform.getWorldScaleX());
        var scaleY = MathHelper.abs(transform.getWorldScaleY());
        var texWidth = MathHelper.abs(textureSource.getWidth());
        var texHeight = MathHelper.abs(textureSource.getHeight());
        return new Rectangle(
                position.getX() - (texWidth * pivot.getX()) * scaleX,
                position.getY() - (texHeight * pivot.getY()) * scaleY,
                texWidth * scaleX,
                texHeight * scaleY);
    }

    /**
     * Get the bounding box of the Sprite (considers rotation).
     *
     * @return The bounding box of the Sprite.
     */
    public Boundary getBoundingBox() {
        var boundingBox = new Boundary(getBoundingRect());
        boundingBox.rotate(new Vector2(boundingBox.getLeft() + (boundingBox.getWidth() / 2f),
                boundingBox.getTop() + (boundingBox.getHeight() / 2f)), getTransform().getRotation());

        return boundingBox;
    }

    /**
     * Get the overlay color of the Sprite.
     *
     * @return The overlay color of the Sprite.
     */
    public Color getOverlayColor() {
        return overlayColor;
    }

    /**
     * Set the overlay color of the Sprite.
     *
     * @param overlayColor The overlay color of the Sprite.
     */
    public void setOverlayColor(Color overlayColor) {
        this.overlayColor = overlayColor;
    }

    /**
     * Get the Texture of the Sprite.
     *
     * @return The Texture of the Sprite.
     */
    public Texture getTexture() {
        return texture;
    }

    /**
     * Set the Texture of the Sprite.
     *
     * @param texture The Texture of the Sprite.
     */
    public void setTexture(Texture texture) {
        this.texture = texture;
        this.textureSource = new Rectangle(0, 0, texture.getWidth(), texture.getHeight());
    }

    /**
     * Get the drawing pivot of the Sprite.
     *
     * @return The pivot of the Sprite.
     */
    public Vector2 getPivot() {
        return pivot;
    }

    /**
     * Set the drawing pivot of the Sprite by the given vector.
     *
     * @param pivot The new pivot values of the Sprite.
     */
    public void setPivot(Vector2 pivot) {
        this.pivot.set(pivot);
    }

    /**
     * Set the drawing pivot of the Sprite by the given values.
     *
     * @param x The new x-axis value of the pivot.
     * @param y The new y-axis value of the pivot.
     */
    public void setPivot(float x, float y) {
        this.pivot.set(x, y);
    }

    /**
     * Get the texture source of the Sprite.
     *
     * @return The texture source of the Sprite.
     */
    public Rectangle getTextureSource() {
        return textureSource;
    }

    /**
     * Set the texture source of the Sprite.
     *
     * @param textureSource The texture source of the Sprite.
     */
    public void setTextureSource(Rectangle textureSource) {
        this.textureSource = textureSource;
    }

    /**
     * Get the sorting depth of the Sprite.
     *
     * @return The sorting depth of the Sprite.
     */
    public int getSortingDepth() {
        return sortingDepth;
    }

    /**
     * Set the sorting depth of the Sprite.
     *
     * @param sortingDepth The sorting depth of the Sprite.
     */
    public void setSortingDepth(int sortingDepth) {
        this.sortingDepth = sortingDepth;
    }
}
