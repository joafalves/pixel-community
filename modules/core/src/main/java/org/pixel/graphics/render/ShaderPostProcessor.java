/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.render;

import org.pixel.commons.DeltaTime;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.content.Texture;
import org.pixel.graphics.Color;
import org.pixel.graphics.shader.Shader;
import org.pixel.graphics.shader.ShaderManager;
import org.pixel.math.Size;

import static org.lwjgl.opengl.GL11.GL_CLAMP;
import static org.lwjgl.opengl.GL15C.glDeleteBuffers;
import static org.lwjgl.opengl.GL30C.*;

public class ShaderPostProcessor implements PostProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(ShaderPostProcessor.class);

    private Shader shader;
    private Color bgColor;
    private Texture texture;
    private Size size;
    private int msfbo;
    private int fbo;
    private int rbo;
    private int vao;
    private int vbo;
    private int stage = 0;
    private float uTime;

    /**
     * Constructor
     *
     * @param shader
     * @param bgColor
     * @param width
     * @param height
     */
    public ShaderPostProcessor(Shader shader, Color bgColor, int width, int height) {
        if (shader == null) {
            throw new RuntimeException("Unable to instantiate post processor without a Shader");
        }

        this.shader = shader;
        this.bgColor = bgColor;
        this.fbo = glGenFramebuffers(); // std frame buffer
        this.msfbo = glGenFramebuffers(); // multi sample frame buffer
        this.rbo = glGenRenderbuffers(); // render buffer
        this.vao = glGenVertexArrays();
        this.vbo = glGenBuffers();
        this.size = new Size(width, height);
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
        glRenderbufferStorageMultisample(GL_RENDERBUFFER, 8, GL_RGB, size.getWidth(), size.getHeight());
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, rbo);
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            LOG.error("Failed to initialize MSFBO");
        }

        if (texture == null) {
            texture = new Texture(glGenTextures());
            texture.bind();
            texture.setTextureWrap(GL_CLAMP, GL_CLAMP);
            texture.setTextureMinMag(GL_NEAREST, GL_NEAREST);

        } else {
            texture.bind();
        }

        // initialize fbo/texture for shader operations
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);
        texture.setData(null, size.getWidth(), size.getHeight());
        texture.unbind();
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture.getId(), 0);
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            LOG.error("Failed to initialize FBO");
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0); // unbind any frame buffer
    }

    /**
     * Setup render data
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
     * Setup shader
     */
    private void setupShader() {
        ShaderManager.useShader(shader);
        glUniform1f(shader.getUniformLocation("uTextureImage"), 0);
    }

    /**
     * Start the post processing phase
     */
    @Override
    public void begin() {
        glBindFramebuffer(GL_FRAMEBUFFER, msfbo);
        glClearColor(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), bgColor.getAlpha());
        glClear(GL_COLOR_BUFFER_BIT); // clear the screen
        stage = 1;
    }

    /**
     * End the post processing phase
     */
    @Override
    public void end() {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, msfbo);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, fbo);
        glBlitFramebuffer(0, 0, size.getWidth(), size.getHeight(), 0, 0, size.getWidth(), size.getHeight(), GL_COLOR_BUFFER_BIT, GL_NEAREST);
        glBindFramebuffer(GL_FRAMEBUFFER, 0); // unbinds both buffers
        stage = 2;
    }

    /**
     * Apply post processing
     * @param delta
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
        ShaderManager.useShader(shader);
        shader.apply();
        if (shader.getUniformLocation("uTime") != null) {
            glUniform1f(shader.getUniformLocation("uTime"), uTime);
        }

        // Render textured quad
        glActiveTexture(GL_TEXTURE0);
        texture.bind();
        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);

        stage = 0;
    }

    /**
     * Dispose function
     */
    @Override
    public void dispose() {
        glDeleteFramebuffers(this.fbo);
        glDeleteFramebuffers(this.msfbo);
        glDeleteRenderbuffers(this.rbo);
        glDeleteBuffers(this.vbo);
        glDeleteVertexArrays(this.vao);
        texture.dispose();
    }

    public int getWidth() {
        return size.getWidth();
    }

    public void setSize(int width, int height) {
        if (width != this.size.getWidth() || height != this.size.getHeight()) {
            this.size.setWidth(width);
            this.size.setHeight(height);
            this.setupBuffers();
        }
    }

    public int getHeight() {
        return size.getHeight();
    }

    public Shader getShader() {
        return shader;
    }

    public void setShader(Shader shader) {
        if (shader != this.shader) {
            this.shader = shader;
            this.setupShader();
        }
    }
}
