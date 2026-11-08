package org.pixel.graphics.backend;

import org.pixel.commons.lifecycle.Disposable;
import org.pixel.graphics.material.Material;

/**
 * The only layer that talks to the GPU API. Everything above it (draw lists, batching, front-ends) is GL-free.
 * <p>
 * Usage per pass: {@link #beginPass(PassState)}, one or more {@link #submit(RenderData)}, then
 * {@link #endPass()}. Implementations record their work into the device's render statistics.
 */
public interface RenderBackend extends Disposable {

    /**
     * @return The limits batching must respect.
     */
    BackendCapabilities capabilities();

    /**
     * @return How many draw calls this backend has made so far (only ever increases; differences measure a pass).
     */
    long getDrawCallCount();

    /**
     * Start a pass: bind the program, apply the view matrix and blend mode.
     *
     * @param state The pass state.
     */
    void beginPass(PassState state);

    /**
     * Upload the vertices and tables, then draw every batch in order.
     *
     * @param data The vertex streams and batches of the pass.
     */
    void submit(RenderData data);

    /**
     * End the pass and restore shared GPU state other renderers rely on.
     */
    void endPass();

    // region render targets and post effects

    /**
     * Create an offscreen target (owned by the caller).
     *
     * @param width  Width in pixels.
     * @param height Height in pixels.
     */
    RenderTarget createTarget(int width, int height);

    /**
     * @return The width in pixels of the screen area being drawn (the window's drawable area).
     */
    int getScreenWidth();

    /**
     * @return The height in pixels of the screen area being drawn (the window's drawable area).
     */
    int getScreenHeight();

    /**
     * Increases whenever the GPU context was lost and recreated (Android can do that when the app is paused): GPU
     * objects made before, such as compiled effects, must then be made again.
     */
    int getContextGeneration();

    /**
     * Compile a full-screen effect. The source defines {@code vec4 effect(vec2 uv)} and may declare its own
     * uniforms; the backend provides {@code uSource}, {@code uInput1..3}, {@code uTexel} (1 / source size in pixels)
     * and {@code uResolution} (destination size). Colors are premultiplied.
     *
     * @param effectSource The GLSL of the effect function (no {@code #version}, no {@code main}).
     */
    PostProgram compilePost(String effectSource);

    /**
     * Draw an effect over the whole destination (outside a pass).
     *
     * @param program     The effect.
     * @param params      Its inputs and uniforms.
     * @param destination The target to write, or {@code null} for the screen.
     * @param blend       {@code false} replaces the destination; {@code true} composites over it (premultiplied).
     */
    void drawPost(PostProgram program, PostParams params, RenderTarget destination, boolean blend);

    /**
     * Read a target's pixels: RGBA8, rows from top to bottom, premultiplied (outside a pass).
     */
    byte[] readPixels(RenderTarget target);

    // endregion

    /**
     * Compile a material's program (called by {@code Material} on first use, after a source change and after the
     * context was recreated). Its batches bind {@link BackendCapabilities#materialTextureSlots()} command textures,
     * plus the material's own textures on the units after them.
     *
     * @param material       The material (its kind and values).
     * @param vertexSource   The vertex shader of a raw material ({@code null} for effect materials).
     * @param source         The effect function, or the raw material's fragment shader.
     * @throws IllegalStateException With the compiler's log if the source does not compile.
     */
    MaterialProgram compileMaterial(Material material, String vertexSource, String source);
}
