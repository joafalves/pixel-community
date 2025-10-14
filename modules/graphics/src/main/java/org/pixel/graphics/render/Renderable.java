package org.pixel.graphics.render;

import org.pixel.commons.Color;
import org.pixel.commons.data.DataMap;
import org.pixel.graphics.shader.Shader;
import org.pixel.math.Matrix4;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;

/**
 * Abstract base class for all renderable objects in the RenderPipeline.
 * Subclasses define specific rendering behavior (sprites, text, custom shapes, etc.).
 * This design allows developers to extend the rendering system without modifying the framework.
 */
public abstract class Renderable implements Cullable {
    protected Vector2 position;
    protected Color color;
    protected int depth;
    protected Shader shader; // Optional custom shader
    protected DataMap shaderData; // Optional custom uniforms

    /**
     * Protected constructor for subclasses.
     */
    protected Renderable() {
        this.position = new Vector2(0, 0);
        this.color = Color.WHITE;
        this.depth = 0;
        this.shaderData = new DataMap();
    }

    /**
     * Render this object using the SpriteBatch (for batched rendering).
     * Default implementation does nothing - override if your renderable supports batching.
     *
     * @param spriteBatch The sprite batch to use.
     */
    public void renderBatched(SpriteBatch spriteBatch) {
        // Default: do nothing. Subclasses override if they support batching.
    }

    /**
     * Render this object using the DirectRenderer (for custom shader rendering).
     * Default implementation does nothing - override if your renderable supports direct rendering.
     *
     * @param directRenderer The direct renderer to use.
     * @param viewMatrix     The camera's view-projection matrix.
     */
    public void renderDirect(DirectRenderer directRenderer, Matrix4 viewMatrix) {
        // Default: do nothing. Subclasses override if they support direct rendering.
    }

    /**
     * Determines if this renderable can be batched (no custom shader).
     *
     * @return True if this renderable can be batched, false otherwise.
     */
    public boolean canBatch() {
        return shader == null;
    }

    /**
     * Gets the world-space bounding box of this renderable for culling.
     * Default implementation returns null (never culled).
     * Subclasses should override to provide accurate bounds.
     *
     * @return The world-space bounding rectangle, or null to disable culling for this object.
     */
    @Override
    public Rectangle getBounds() {
        return null; // Default: never culled
    }

    //<editor-fold desc="Common Getters and Setters">
    public Vector2 getPosition() { return position; }
    public Renderable setPosition(Vector2 position) { this.position = position; return this; }
    public Renderable setPosition(float x, float y) { this.position.set(x, y); return this; }
    public Renderable setPosition(float xy) { this.position.set(xy, xy); return this; }

    public Color getColor() { return color; }
    public Renderable setColor(Color color) { this.color = color; return this; }

    public int getDepth() { return depth; }
    public Renderable setDepth(int depth) { this.depth = depth; return this; }

    public Shader getShader() { return shader; }
    public Renderable setShader(Shader shader) { this.shader = shader; return this; }

    public DataMap getShaderData() { return shaderData; }
    public Renderable setShaderData(DataMap shaderData) { this.shaderData = shaderData; return this; }
    //</editor-fold>

    //<editor-fold desc="Fluent Uniform Setters for Custom Shaders">

    /**
     * Sets a float uniform value for the custom shader.
     *
     * @param name The name of the uniform.
     * @param value The value.
     * @return This renderable for chaining.
     */
    public Renderable setUniform(String name, float value) {
        this.shaderData.put(name, value);
        return this;
    }

    /**
     * Sets an integer uniform value for the custom shader.
     *
     * @param name The name of the uniform.
     * @param value The value.
     * @return This renderable for chaining.
     */
    public Renderable setUniform(String name, int value) {
        this.shaderData.put(name, value);
        return this;
    }

    /**
     * Sets a Vector2 uniform value for the custom shader.
     *
     * @param name The name of the uniform.
     * @param value The value.
     * @return This renderable for chaining.
     */
    public Renderable setUniform(String name, Vector2 value) {
        this.shaderData.put(name, value);
        return this;
    }

    //</editor-fold>
}
