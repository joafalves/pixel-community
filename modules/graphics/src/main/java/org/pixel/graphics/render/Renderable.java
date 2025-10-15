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
 * 
 * <p>Uses generics to specify which renderer type this renderable requires:
 * <ul>
 *   <li>{@code Renderable<SpriteBatch>} - Uses batched sprite rendering</li>
 *   <li>{@code Renderable<SdfShapeRenderer>} - Uses SDF shape rendering</li>
 *   <li>{@code Renderable<SdfTextRenderer>} - Uses SDF text rendering</li>
 * </ul>
 * 
 * <p>The RenderPipeline automatically provides the correct renderer instance based on the generic type.
 * This design allows developers to extend the rendering system without modifying the framework.
 *
 * @param <R> The type of renderer this renderable uses
 */
public abstract class Renderable<R extends Renderer> implements Cullable {
    protected Vector2 position;
    protected Color color;
    protected int depth;
    protected Shader shader; // Optional custom shader
    protected DataMap shaderData; // Optional custom uniforms
    protected final Class<R> rendererType;

    /**
     * Protected constructor for subclasses.
     *
     * @param rendererType The class of the renderer this renderable uses
     */
    protected Renderable(Class<R> rendererType) {
        this.position = new Vector2(0, 0);
        this.color = Color.WHITE;
        this.depth = 0;
        this.shaderData = new DataMap();
        this.rendererType = rendererType;
    }

    /**
     * Render this object using its specific renderer type.
     * Subclasses must implement this to define their rendering behavior.
     *
     * @param renderer   The renderer instance (guaranteed to be of type R)
     * @param viewMatrix The camera's view-projection matrix
     */
    public abstract void render(R renderer, Matrix4 viewMatrix);

    /**
     * Gets the renderer type this renderable requires.
     *
     * @return The class of the renderer type
     */
    public Class<R> getRendererType() {
        return rendererType;
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
    public Renderable<R> setPosition(Vector2 position) { this.position = position; return this; }
    public Renderable<R> setPosition(float x, float y) { this.position.set(x, y); return this; }
    public Renderable<R> setPosition(float xy) { this.position.set(xy, xy); return this; }

    public Color getColor() { return color; }
    public Renderable<R> setColor(Color color) { this.color = color; return this; }

    public int getDepth() { return depth; }
    public Renderable<R> setDepth(int depth) { this.depth = depth; return this; }

    public Shader getShader() { return shader; }
    public Renderable<R> setShader(Shader shader) { this.shader = shader; return this; }

    public DataMap getShaderData() { return shaderData; }
    public Renderable<R> setShaderData(DataMap shaderData) { this.shaderData = shaderData; return this; }
    //</editor-fold>

    //<editor-fold desc="Fluent Uniform Setters for Custom Shaders">

    /**
     * Sets a float uniform value for the custom shader.
     *
     * @param name The name of the uniform.
     * @param value The value.
     * @return This renderable for chaining.
     */
    public Renderable<R> setUniform(String name, float value) {
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
    public Renderable<R> setUniform(String name, int value) {
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
    public Renderable<R> setUniform(String name, Vector2 value) {
        this.shaderData.put(name, value);
        return this;
    }

    //</editor-fold>
}
