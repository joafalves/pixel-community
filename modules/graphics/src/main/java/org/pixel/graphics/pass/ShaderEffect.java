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

    private final String source;
    private final PostParams params = new PostParams();
    private PostProgram program;
    private RenderBackend compiledFor;
    private int compiledGeneration;

    /**
     * @param source GLSL defining {@code vec4 effect(vec2 uv)} (and its own uniforms).
     */
    public ShaderEffect(String source) {
        if (source == null || !source.contains("effect")) {
            throw new IllegalArgumentException("An effect source must define vec4 effect(vec2 uv).");
        }
        this.source = source;
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
        RenderBackend backend = context.backend();
        if (program == null || compiledFor != backend || compiledGeneration != backend.getContextGeneration()) {
            if (program != null && compiledFor == backend && compiledGeneration == backend.getContextGeneration()) {
                program.dispose();
            } // otherwise it belongs to another (or a lost) context: freed with it, never deleted from this one
            program = backend.compilePost(this.source);
            compiledFor = backend;
            compiledGeneration = backend.getContextGeneration();
        }
        params.input(0, source);
        backend.drawPost(program, params, destination, blend);
    }

    @Override
    public void dispose() {
        if (program != null) {
            if (compiledGeneration == compiledFor.getContextGeneration()) {
                program.dispose();
            }
            program = null;
            compiledFor = null;
        }
    }
}
