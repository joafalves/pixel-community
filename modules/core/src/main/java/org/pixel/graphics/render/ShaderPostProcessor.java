/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.render;

import static org.lwjgl.opengl.GL11C.GL_ONE;
import static org.lwjgl.opengl.GL11C.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11C.GL_RGBA;
import static org.lwjgl.opengl.GL11C.GL_RGBA8;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11C.glBindTexture;
import static org.lwjgl.opengl.GL11C.glBlendFunc;
import static org.lwjgl.opengl.GL11C.glTexImage2D;
import static org.lwjgl.opengl.GL11C.glTexParameteri;
import static org.lwjgl.opengl.GL12C.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL15C.glDeleteBuffers;
import static org.lwjgl.opengl.GL30C.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL30C.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL30C.GL_DRAW_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30C.GL_FLOAT;
import static org.lwjgl.opengl.GL30C.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30C.GL_FRAMEBUFFER_COMPLETE;
import static org.lwjgl.opengl.GL30C.GL_NEAREST;
import static org.lwjgl.opengl.GL30C.GL_READ_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30C.GL_RENDERBUFFER;
import static org.lwjgl.opengl.GL30C.GL_RGB;
import static org.lwjgl.opengl.GL30C.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL30C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL30C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL30C.GL_TRIANGLES;
import static org.lwjgl.opengl.GL30C.glActiveTexture;
import static org.lwjgl.opengl.GL30C.glBindBuffer;
import static org.lwjgl.opengl.GL30C.glBindFramebuffer;
import static org.lwjgl.opengl.GL30C.glBindRenderbuffer;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL30C.glBlitFramebuffer;
import static org.lwjgl.opengl.GL30C.glBufferData;
import static org.lwjgl.opengl.GL30C.glCheckFramebufferStatus;
import static org.lwjgl.opengl.GL30C.glClear;
import static org.lwjgl.opengl.GL30C.glDeleteFramebuffers;
import static org.lwjgl.opengl.GL30C.glDeleteRenderbuffers;
import static org.lwjgl.opengl.GL30C.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30C.glDrawArrays;
import static org.lwjgl.opengl.GL30C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30C.glFramebufferRenderbuffer;
import static org.lwjgl.opengl.GL30C.glFramebufferTexture2D;
import static org.lwjgl.opengl.GL30C.glGenBuffers;
import static org.lwjgl.opengl.GL30C.glGenFramebuffers;
import static org.lwjgl.opengl.GL30C.glGenRenderbuffers;
import static org.lwjgl.opengl.GL30C.glGenTextures;
import static org.lwjgl.opengl.GL30C.glGenVertexArrays;
import static org.lwjgl.opengl.GL30C.glRenderbufferStorageMultisample;
import static org.lwjgl.opengl.GL30C.glUniform1f;
import static org.lwjgl.opengl.GL30C.glVertexAttribPointer;

import java.nio.ByteBuffer;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.content.Texture;
import org.pixel.graphics.shader.Shader;
import org.pixel.math.SizeInt;

public class ShaderPostProcessor implements PostProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(ShaderPostProcessor.class);

    private Shader shader;
    private Texture texture;
    private final SizeInt sizeInt;
    private final int msfbo;
    private final int fbo;
    private final int rbo;
    private final int vao;
    private final int vbo;
    private int stage = 0;
    private float uTime;

    /**
     * Constructor.
     *
     * @param shader The shader to use.
     * @param width  The width of the post-processed texture.
     * @param height The height of the post-processed texture.
     */
    public ShaderPostProcessor(Shader shader, int width, int height) {
        if (shader == null) {
            throw new RuntimeException("Unable to instantiate post processor without a Shader");
        }

        this.shader = shader;
        this.fbo = glGenFramebuffers(); // std frame buffer
        this.msfbo = glGenFramebuffers(); // multi sample frame buffer
        this.rbo = glGenRenderbuffers(); // render buffer
        this.vao = glGenVertexArrays();
        this.vbo = glGenBuffers();
        this.sizeInt = new SizeInt(width, height);
        this.setupBuffers();
        this.setupRenderData();
        this.setupShader();
    }

    /**
     * Setup buffers (call only when size is changed)
     */
    private void setupBuffers() {
        // initialize render buffer with a multi sampled fbo
        glBindFramebuffer(GL_FRAMEBUFFER, msfbo);
        glBindRenderbuffer(GL_RENDERBUFFER, rbo);
        glRenderbufferStorageMultisample(GL_RENDERBUFFER, 8, GL_RGB, sizeInt.getWidth(), sizeInt.getHeight());
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, rbo);
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            LOG.error("Failed to initialize MSFBO.");
        }

        if (texture == null) {
            texture = new Texture(glGenTextures());
            glBindTexture(GL_TEXTURE_2D, texture.getId());
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        } else {
            glBindTexture(GL_TEXTURE_2D, texture.getId());
        }

        // initialize fbo/texture for shader operations
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, sizeInt.getWidth(), sizeInt.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE,
                (ByteBuffer) null);
        glBindTexture(GL_TEXTURE_2D, 0);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture.getId(), 0);
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            LOG.error("Failed to initialize FBO.");
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0); // unbind any frame buffer
    }

    /**
     * Setup render data.
     */
    private void setupRenderData() {
        float[] vertices = new float[]{
                // Pos       // Tex
                -1.0f, -1.0f, 0.0f, 0.0f,
                1.0f, 1.0f, 1.0f, 1.0f,
                -1.0f, 1.0f, 0.0f, 1.0f,

                -1.0f, -1.0f, 0.0f, 0.0f,
                1.0f, -1.0f, 1.0f, 0.0f,
                1.0f, 1.0f, 1.0f, 1.0f
        };

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glBindVertexArray(vao);
        glEnableVertexAttribArray(shader.getAttributeLocation("aVertexPosition"));
        glVertexAttribPointer(shader.getAttributeLocation("aVertexPosition"), 4, GL_FLOAT, false, 4 * Float.BYTES, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0); // unbind
    }

    /**
     * Setup shader.
     */
    private void setupShader() {
        shader.use();
        glUniform1f(shader.getUniformLocation("uTextureImage"), 0);
    }

    /**
     * Start the post-processing phase.
     */
    @Override
    public void begin() {
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        glBindFramebuffer(GL_FRAMEBUFFER, msfbo);
        glClear(GL_COLOR_BUFFER_BIT);
        stage = 1;
    }

    /**
     * End the post-processing phase.
     */
    @Override
    public void end() {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, msfbo);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, fbo);
        glBlitFramebuffer(0, 0, sizeInt.getWidth(), sizeInt.getHeight(), 0, 0, sizeInt.getWidth(),
                sizeInt.getHeight(), GL_COLOR_BUFFER_BIT, GL_NEAREST);
        glBindFramebuffer(GL_FRAMEBUFFER, 0); // unbinds both buffers
        stage = 2;
    }

    /**
     * Apply post-processing.
     *
     * @param delta Time since last frame.
     */
    @Override
    public void apply(DeltaTime delta) {
        // to make things practical:
        if (stage == 0) {
            begin();
            end();

        } else if (stage == 1) {
            end();
        }

        uTime += delta.getElapsed();
        shader.use();
        shader.apply();
        if (shader.getUniformLocation("uTime") != null) {
            glUniform1f(shader.getUniformLocation("uTime"), uTime);
        }

        // Render textured quad
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture.getId());

        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);

        stage = 0;
    }

    @Override
    public void dispose() {
        glDeleteFramebuffers(this.fbo);
        glDeleteFramebuffers(this.msfbo);
        glDeleteRenderbuffers(this.rbo);
        glDeleteBuffers(this.vbo);
        glDeleteVertexArrays(this.vao);
        texture.dispose();
    }

    /**
     * Set post-processing texture size.
     *
     * @param width  Width.
     * @param height Height.
     */
    public void setSize(int width, int height) {
        if (width != this.sizeInt.getWidth() || height != this.sizeInt.getHeight()) {
            this.sizeInt.setWidth(width);
            this.sizeInt.setHeight(height);
            this.setupBuffers();
        }
    }

    /**
     * Get texture width.
     *
     * @return Width.
     */
    public int getWidth() {
        return sizeInt.getWidth();
    }

    /**
     * Get texture height.
     *
     * @return Height.
     */
    public int getHeight() {
        return sizeInt.getHeight();
    }

    /**
     * Get the post-processing shader.
     *
     * @return Shader.
     */
    public Shader getShader() {
        return shader;
    }

    /**
     * Set post-processing shader.
     *
     * @param shader Shader.
     */
    public void setShader(Shader shader) {
        if (shader != this.shader) {
            this.shader = shader;
            this.setupShader();
        }
    }
}
