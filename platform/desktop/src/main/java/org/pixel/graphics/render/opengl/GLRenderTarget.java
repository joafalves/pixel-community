package org.pixel.graphics.render.opengl;

import org.pixel.content.Texture;
import org.pixel.content.opengl.GLTexture;
import org.pixel.graphics.render.RenderTarget;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL30C.*;

/**
 * An OpenGL implementation of a RenderTarget.
 */
public class GLRenderTarget implements RenderTarget {

    private final int fboId;
    private final int rboId;
    private final int resolveFboId; // FBO for Y-flipped resolved image
    private final GLTexture texture;
    private final int[] previousViewport = new int[4];

    /**
     * Constructor.
     *
     * @param width The width of the render target.
     * @param height The height of the render target.
     */
    public GLRenderTarget(int width, int height) {
        // Create the final texture (correctly oriented for texture coordinates)
        this.texture = new GLTexture(width, height);
        
        // Initialize the texture with proper parameters
        glBindTexture(GL_TEXTURE_2D, texture.getId());
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glBindTexture(GL_TEXTURE_2D, 0);

        // Create the render FBO (with temporary texture for rendering)
        this.fboId = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, fboId);

        // Create temporary texture for rendering
        int renderTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, renderTexture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, texture.getWidth(), texture.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glBindTexture(GL_TEXTURE_2D, 0);

        // Attach render texture to render FBO
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, renderTexture, 0);

        // Create RBO for depth/stencil
        this.rboId = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, rboId);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, texture.getWidth(), texture.getHeight());
        glBindRenderbuffer(GL_RENDERBUFFER, 0);

        // Attach RBO to render FBO
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, rboId);

        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Failed to create render framebuffer");
        }

        // Create the resolve FBO (for the final, correctly oriented texture)
        this.resolveFboId = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, resolveFboId);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture.getId(), 0);

        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Failed to create resolve framebuffer");
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    @Override
    public int getWidth() {
        return texture.getWidth();
    }

    @Override
    public int getHeight() {
        return texture.getHeight();
    }

    @Override
    public Texture getTexture() {
        return texture;
    }

    @Override
    public void begin() {
        // Save the current viewport
        glGetIntegerv(GL_VIEWPORT, previousViewport);
        
        glBindFramebuffer(GL_FRAMEBUFFER, fboId);
        glViewport(0, 0, getWidth(), getHeight());
        
        // Clear the render target
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
    }

    @Override
    public void end() {
        // Blit from render FBO to resolve FBO with Y-flip to match texture coordinate convention
        glBindFramebuffer(GL_READ_FRAMEBUFFER, fboId);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, resolveFboId);
        
        // Flip Y by reversing source and destination Y coordinates
        glBlitFramebuffer(
            0, 0, texture.getWidth(), texture.getHeight(),      // src: bottom-left origin (OpenGL framebuffer)
            0, texture.getHeight(), texture.getWidth(), 0,       // dst: flipped to match texture coordinates (top-left origin)
            GL_COLOR_BUFFER_BIT, GL_NEAREST
        );
        
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        
        // Restore the previous viewport
        glViewport(previousViewport[0], previousViewport[1], previousViewport[2], previousViewport[3]);
    }

    @Override
    public void dispose() {
        glDeleteFramebuffers(fboId);
        glDeleteFramebuffers(resolveFboId);
        glDeleteRenderbuffers(rboId);
        texture.dispose();
    }
}
