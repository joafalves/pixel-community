package org.pixel.graphics.render;

import org.pixel.content.Texture;
import org.pixel.math.Matrix4;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;

/**
 * A renderable for drawing sprites (textured quads).
 */
public class SpriteRenderable extends Renderable {
    private Texture texture;
    private Rectangle source;
    private Vector2 anchor;
    private Vector2 scale;
    private float rotation;
    private Rectangle cachedBounds; // Cached bounding box for culling

    /**
     * Public constructor.
     */
    public SpriteRenderable() {
        super();
        this.scale = new Vector2(1, 1);
        this.anchor = Vector2.ZERO;
        this.rotation = 0f;
        this.cachedBounds = null;
    }

    @Override
    public void renderBatched(SpriteBatch spriteBatch) {
        spriteBatch.draw(texture, position, source, color, anchor, scale.getX(), scale.getY(), rotation, depth);
    }

    @Override
    public void renderDirect(DirectRenderer directRenderer, Matrix4 viewMatrix) {
        directRenderer.draw(this, viewMatrix);
    }

    @Override
    public Rectangle getBounds() {
        if (texture == null) {
            return null;
        }

        float width, height;
        if (source != null) {
            width = source.getWidth() * scale.getX();
            height = source.getHeight() * scale.getY();
        } else {
            width = texture.getWidth() * scale.getX();
            height = texture.getHeight() * scale.getY();
        }

        float x = position.getX() - (width * anchor.getX());
        float y = position.getY() - (height * anchor.getY());

        if (rotation != 0f) {
            float halfW = width * 0.5f;
            float halfH = height * 0.5f;
            float centerX = x + halfW;
            float centerY = y + halfH;

            float maxExtent = (float) Math.sqrt(halfW * halfW + halfH * halfH);

            if (cachedBounds == null) {
                cachedBounds = new Rectangle(centerX - maxExtent, centerY - maxExtent, maxExtent * 2, maxExtent * 2);
            } else {
                cachedBounds.set(centerX - maxExtent, centerY - maxExtent, maxExtent * 2, maxExtent * 2);
            }
            return cachedBounds;
        }

        if (cachedBounds == null) {
            cachedBounds = new Rectangle(x, y, width, height);
        } else {
            cachedBounds.set(x, y, width, height);
        }
        return cachedBounds;
    }

    //<editor-fold desc="Sprite-Specific Getters and Setters">
    public Texture getTexture() { return texture; }
    public SpriteRenderable setTexture(Texture texture) { this.texture = texture; return this; }

    public Rectangle getSource() { return source; }
    public SpriteRenderable setSource(Rectangle source) { this.source = source; return this; }

    public Vector2 getAnchor() { return anchor; }
    public SpriteRenderable setAnchor(Vector2 anchor) { this.anchor = anchor; return this; }
    public SpriteRenderable setAnchor(float x, float y) { this.anchor.set(x, y); return this; }
    public SpriteRenderable setAnchor(float xy) { this.anchor.set(xy, xy); return this; }

    public Vector2 getScale() { return scale; }
    public SpriteRenderable setScale(Vector2 scale) { this.scale = scale; return this; }
    public SpriteRenderable setScale(float x, float y) { this.scale.set(x, y); return this; }
    public SpriteRenderable setScale(float xy) { this.scale.set(xy, xy); return this; }

    public float getRotation() { return rotation; }
    public SpriteRenderable setRotation(float rotation) { this.rotation = rotation; return this; }
    //</editor-fold>

    //<editor-fold desc="Fluent Setters Override for Method Chaining">
    @Override
    public SpriteRenderable setPosition(Vector2 position) { super.setPosition(position); return this; }
    @Override
    public SpriteRenderable setPosition(float x, float y) { super.setPosition(x, y); return this; }
    @Override
    public SpriteRenderable setPosition(float xy) { super.setPosition(xy); return this; }

    @Override
    public SpriteRenderable setColor(org.pixel.commons.Color color) { super.setColor(color); return this; }

    @Override
    public SpriteRenderable setDepth(int depth) { super.setDepth(depth); return this; }

    @Override
    public SpriteRenderable setShader(org.pixel.graphics.shader.Shader shader) { super.setShader(shader); return this; }

    @Override
    public SpriteRenderable setShaderData(org.pixel.commons.data.DataMap shaderData) { super.setShaderData(shaderData); return this; }

    @Override
    public SpriteRenderable setUniform(String name, float value) { super.setUniform(name, value); return this; }

    @Override
    public SpriteRenderable setUniform(String name, int value) { super.setUniform(name, value); return this; }

    @Override
    public SpriteRenderable setUniform(String name, Vector2 value) { super.setUniform(name, value); return this; }
    //</editor-fold>
}
