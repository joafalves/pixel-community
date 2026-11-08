package org.pixel.graphics.opengl.render;

import org.pixel.commons.Color;
import org.pixel.content.Texture;
import org.pixel.content.opengl.GLTexture;
import org.pixel.graphics.backend.RenderTarget;
import org.pixel.graphics.opengl.GLApi;

import static org.pixel.graphics.opengl.GLApi.*;

/**
 * A framebuffer object with one RGBA8 color texture (linear filtering, clamped). Rows are stored top first (the
 * backend flips passes drawn into it), so the texture samples like an image texture.
 */
final class GLRenderTarget implements RenderTarget {

    private final GLApi gl;
    private final Color clearColor = new Color(0f, 0f, 0f, 0f);
    private int framebuffer;
    private TargetTexture texture;
    private boolean disposed;
    private int generation; // context generation the GL objects belong to
    private final java.util.function.IntSupplier currentGeneration; // the backend's

    GLRenderTarget(GLApi gl, int width, int height, int generation, java.util.function.IntSupplier currentGeneration) {
        this.gl = gl;
        this.generation = generation;
        this.currentGeneration = currentGeneration;
        allocate(width, height);
    }

    /** Whether the GL names belong to a context that was lost (then they must never be deleted: they may be reused). */
    private boolean stale() {
        return generation != currentGeneration.getAsInt();
    }

    /**
     * After the context was recreated the GL names are dead: allocate new ones (the old ones are not deleted, they
     * may already belong to something else in the new context).
     */
    void ensureGeneration(int current) {
        if (generation != current) {
            generation = current;
            allocate(getWidth(), getHeight());
        }
    }

    private void allocate(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Render target size must be positive: " + width + "x" + height);
        }
        int previous = gl.getInteger(GL_FRAMEBUFFER_BINDING);
        int previousTexture = gl.getInteger(GL_TEXTURE_BINDING_2D); // keep the active unit's binding as it was
        int id = gl.genTexture();
        gl.bindTexture(GL_TEXTURE_2D, id);
        gl.texImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, GL_RGBA, GL_UNSIGNED_BYTE, null);
        gl.texParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        gl.texParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        gl.texParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        gl.texParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        gl.bindTexture(GL_TEXTURE_2D, previousTexture);

        framebuffer = gl.genFramebuffer();
        gl.bindFramebuffer(GL_FRAMEBUFFER, framebuffer);
        gl.framebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, id, 0);
        int status = gl.checkFramebufferStatus(GL_FRAMEBUFFER);
        gl.bindFramebuffer(GL_FRAMEBUFFER, previous);
        if (status != GL_FRAMEBUFFER_COMPLETE) {
            gl.deleteFramebuffer(framebuffer);
            gl.deleteTexture(id);
            throw new IllegalStateException("Render target framebuffer is incomplete (status 0x"
                    + Integer.toHexString(status) + ").");
        }
        if (texture == null) {
            texture = new TargetTexture(gl, id, width, height);
        } else {
            texture.replace(id, width, height);
        }
    }

    int framebuffer() {
        return framebuffer;
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
    public void resize(int width, int height) {
        checkActive();
        if (stale()) { // a new context: allocate there, at the new size (the old names died with their context)
            generation = currentGeneration.getAsInt();
        } else if (width == getWidth() && height == getHeight()) {
            return;
        } else {
            gl.deleteFramebuffer(framebuffer);
            gl.deleteTexture(texture.getId());
        }
        try {
            allocate(width, height);
        } catch (RuntimeException e) {
            disposed = true; // the old names are gone and the new ones failed: nothing left to delete later
            throw e;
        }
    }

    @Override
    public Color getClearColor() {
        return clearColor;
    }

    @Override
    public void setClearColor(Color color) {
        clearColor.set(color != null ? color : Color.TRANSPARENT);
    }

    private void checkActive() {
        if (disposed) {
            throw new IllegalStateException("Render target has been disposed.");
        }
    }

    boolean isDisposed() {
        return disposed;
    }

    @Override
    public void dispose() {
        if (!disposed) {
            disposed = true;
            if (!stale()) {
                gl.deleteFramebuffer(framebuffer);
                gl.deleteTexture(texture.getId());
            }
        }
    }

    /**
     * The target's texture: premultiplied, and its GL name changes on resize (the object stays the same, so
     * references held by games stay valid). Disposing it is the target's job.
     */
    private static final class TargetTexture extends GLTexture {
        private int currentId;

        TargetTexture(GLApi gl, int id, int width, int height) {
            super(gl, id, width, height, true);
            this.currentId = id;
        }

        void replace(int id, int width, int height) {
            currentId = id;
            setSize(width, height);
        }

        @Override
        public int getId() {
            return currentId;
        }

        @Override
        public void dispose() {
            // owned by the render target
        }
    }
}
