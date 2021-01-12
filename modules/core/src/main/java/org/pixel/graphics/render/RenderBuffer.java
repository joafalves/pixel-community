/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.render;

import org.pixel.commons.lifecycle.Disposable;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.graphics.shader.Shader;
import org.pixel.graphics.shader.ShaderManager;
import org.pixel.graphics.shader.standard.RenderBufferShader;
import org.pixel.content.Texture;
import org.pixel.math.Rectangle;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_STENCIL_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL11C.GL_LINEAR;
import static org.lwjgl.opengl.GL11C.GL_RGBA;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11C.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11C.glClear;
import static org.lwjgl.opengl.GL11C.glDrawArrays;
import static org.lwjgl.opengl.GL11C.glGenTextures;
import static org.lwjgl.opengl.GL30C.*;

public class RenderBuffer implements Disposable {

    private static final Logger LOG = LoggerFactory.getLogger(RenderBuffer.class);

    private Shader shader;
    private Texture texture;
    private Rectangle sourceArea;
    private int msfbo;
    private int fbo;
    private int rbo;
    private int vao;
    private int vbo;

    /**
     * Constructor
     *
     * @param sourceArea
     */
    public RenderBuffer(Rectangle sourceArea) {
        this.sourceArea = sourceArea;
        this.init();
    }

    /**
     * Setup render buffer
     */
    private void setupRenderBuffer() {
        glBindFramebuffer(GL_FRAMEBUFFER, msfbo);
        glBindRenderbuffer(GL_RENDERBUFFER, rbo);
        glRenderbufferStorageMultisample(GL_RENDERBUFFER, 4, GL_RGBA, (int) sourceArea.getWidth(), (int) sourceArea.getHeight());
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, rbo);
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            LOG.error("Failed to initialize MSFBO");
        }

        if (texture == null) {
            texture = new Texture(glGenTextures());
            texture.bind();
            texture.setTextureWrap(GL_CLAMP, GL_CLAMP);
            texture.setTextureMinMag(GL_LINEAR, GL_LINEAR);

        } else {
            texture.bind();
        }

        glBindFramebuffer(GL_FRAMEBUFFER, fbo);
        texture.setData(null, (int) sourceArea.getWidth(), (int) sourceArea.getHeight());
        texture.unbind();
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture.getId(), 0);
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            LOG.error("Failed to initialize FBO");
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0); // unbind any frame buffer
    }

    /**
     * Initialize
     */
    private void init() {
        this.shader = new RenderBufferShader();
        this.fbo = glGenFramebuffers();
        this.msfbo = glGenFramebuffers();
        this.rbo = glGenRenderbuffers();
        this.vao = glGenVertexArrays();
        this.vbo = glGenBuffers();

        // initialize render buffer:
        setupRenderBuffer();

        // setup render data:
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
        glVertexAttribPointer(shader.getAttributeLocation("aVertexPosition"), 4, GL_FLOAT, false,
                4 * Float.BYTES, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0); // unbind

        // setup shader
        ShaderManager.useShader(shader);
        glUniform1f(shader.getUniformLocation("uTextureImage"), 0);
    }

    /**
     * Start capturing render data
     */
    public void begin() {
        glBindFramebuffer(GL_FRAMEBUFFER, msfbo);
        //glClearColor(0.0f, 0.0f, 0.5f, 1f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT); // clear the screen
    }

    /**
     * Stop capturing render data
     */
    public void end() {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, msfbo);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, fbo);
        glBlitFramebuffer(0, 0, (int) sourceArea.getWidth(), (int) sourceArea.getHeight(),
                0, 0, (int) sourceArea.getWidth(), (int) sourceArea.getHeight(), GL_COLOR_BUFFER_BIT, GL_LINEAR);
        glBindFramebuffer(GL_FRAMEBUFFER, 0); // unbinds both buffers
    }

    /**
     * Draw captured data
     */
    public void draw() {
        ShaderManager.useShader(shader);

        // Render textured quad
        glActiveTexture(GL_TEXTURE0);
        texture.bind();
        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);
    }

    /**
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public void setSourceArea(float x, float y, float width, float height) {
        if (sourceArea.getWidth() != width || sourceArea.getHeight() != height) {
            this.sourceArea.set(x, y, width, height);
            this.setupRenderBuffer();
        }
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
}
