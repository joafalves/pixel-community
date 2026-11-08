package org.pixel.graphics.pass;

import org.pixel.commons.Color;
import org.pixel.content.Texture;
import org.pixel.graphics.backend.PostParams;
import org.pixel.graphics.backend.PostProgram;
import org.pixel.graphics.backend.RenderBackend;
import org.pixel.graphics.backend.RenderTarget;

/**
 * A one-pass {@link PostEffect} written in GLSL: a function {@code vec4 effect(vec2 uv)} that returns the
 * (premultiplied) color of the output pixel. Available to it: {@code uSource} (the image), {@code uInput1..3},
 * {@code uTexel} (1 / source size in pixels) and {@code uResolution} (output size in pixels), plus any uniforms it
 * declares and sets with {@link #set}. The same source runs on OpenGL 3.3 and OpenGL ES 3.0.
 * <pre>
 * ShaderEffect invert = new ShaderEffect("""
 *         vec4 effect(vec2 uv) {
 *             vec4 c = texture(uSource, uv);
 *             return vec4(c.a - c.rgb, c.a); // premultiplied
 *         }
 *         """);
 * </pre>
 * Compiled on first use. Not thread-safe.
 */
public class ShaderEffect extends PostEffect {

    private static final org.pixel.commons.logger.Logger log =
            org.pixel.commons.logger.LoggerFactory.getLogger(ShaderEffect.class);

    private String source;
    private int version;
    private final PostParams params = new PostParams();
    private PostProgram program;
    private RenderBackend compiledFor;
    private int compiledGeneration;
    private int compiledVersion;
    private int failedVersion = -1;
    private String error;
    private boolean disposed;

    /**
     * @param source GLSL defining {@code vec4 effect(vec2 uv)} (and its own uniforms).
     */
    public ShaderEffect(String source) {
        this.source = check(source);
    }

    private static String check(String source) {
        if (source == null || !source.contains("effect")) {
            throw new IllegalArgumentException("An effect source must define vec4 effect(vec2 uv).");
        }
        return source;
    }

    /**
     * Replace the effect's source (hot reload); compiled on next use. If it fails to compile, the previous program
     * keeps running and {@link #getError()} says why.
     */
    public void setSource(String source) {
        this.source = check(source);
        version++;
    }

    /**
     * @return Why the last source failed to compile (the previous program is still in use), or {@code null}.
     */
    public String getError() {
        return error;
    }

    public ShaderEffect set(String uniform, float x) {
        params.set(uniform, x);
        return this;
    }

    public ShaderEffect set(String uniform, float x, float y) {
        params.set(uniform, x, y);
        return this;
    }

    public ShaderEffect set(String uniform, float x, float y, float z, float w) {
        params.set(uniform, x, y, z, w);
        return this;
    }

    /** Set a vec4 uniform to a color (straight alpha). */
    public ShaderEffect set(String uniform, Color color) {
        if (color == null) {
            throw new IllegalArgumentException("Color cannot be null.");
        }
        return set(uniform, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    /**
     * Set an extra input texture ({@code uInput1..3}).
     */
    public ShaderEffect input(int index, Texture texture) {
        if (index < 1 || index >= PostParams.MAX_INPUTS) {
            throw new IllegalArgumentException("Extra inputs are 1 to " + (PostParams.MAX_INPUTS - 1) + ".");
        }
        params.input(index, texture);
        return this;
    }

    @Override
    protected void apply(PostContext context, Texture source, RenderTarget destination, boolean blend) {
        params.input(0, source);
        context.backend().drawPost(program(context.backend()), params, destination, blend);
    }

    /**
     * The program for this backend: compiled on first use, and again after a source change or a lost context. A
     * program of a live context is released when replaced; one of another backend or a lost context never is (it
     * belongs to that context). A source that fails keeps the previous program; with none, the failure is thrown.
     */
    private PostProgram program(RenderBackend backend) {
        if (disposed) {
            throw new IllegalStateException("Effect has been disposed.");
        }
        int generation = backend.getContextGeneration();
        boolean live = program != null && compiledFor == backend && compiledGeneration == generation;
        if (live && (compiledVersion == version || failedVersion == version)) {
            return program;
        }
        try {
            PostProgram fresh = backend.compilePost(this.source);
            if (live) {
                program.dispose();
            }
            program = fresh;
            compiledFor = backend;
            compiledGeneration = generation;
            compiledVersion = version;
            error = null;
        } catch (RuntimeException e) {
            if (!live) {
                throw e;
            }
            failedVersion = version;
            error = e.getMessage();
            log.error("Effect source failed to compile; keeping the previous version.\n{0}", error);
        }
        return program;
    }

    /**
     * Release the program. The effect can't be used afterwards (an effect shared by several pipelines is disposed
     * by each: dispose it once, yourself, or don't share it).
     */
    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        if (program != null && compiledGeneration == compiledFor.getContextGeneration()) {
            program.dispose();
        }
        program = null;
        compiledFor = null;
    }
}
