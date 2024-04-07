package org.pixel.graphics.opengl;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.lifecycle.State;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.graphics.Game;
import org.pixel.graphics.GraphicsDevice;

public class GLESGraphicsDevice extends GLSurfaceView implements GraphicsDevice {

    private static final Logger log = LoggerFactory.getLogger(GLESGraphicsDevice.class);

    private final Game game;

    private State state;

    public GLESGraphicsDevice(Game game, Context context) {
        super(context);
        this.game = game;
        this.state = State.CREATED;
    }

    @Override
    public boolean init() {
        if (this.state.hasInitialized()) {
            log.warn("Graphics device already initialized.");
            return false;
        }

        log.debug("Initializing OpenGL ES graphics device...");
        this.state = State.INITIALIZING;

        this.setEGLContextClientVersion(2); // Use OpenGL ES 2.0 or higher as appropriate
        this.setRenderer(new GLSurfaceView.Renderer() {
            private final DeltaTime delta = new DeltaTime();

            @Override
            public void onSurfaceCreated(javax.microedition.khronos.opengles.GL10 gl, javax.microedition.khronos.egl.EGLConfig config) {
                initGLESCapabilities();
                setClearColor(game.getSettings().getBackgroundColor());
            }

            @Override
            public void onDrawFrame(javax.microedition.khronos.opengles.GL10 gl) {
                // android game loop:
                delta.tick();
                game.update(delta);
                if (game.getSettings().isAutoClear()) {
                    clear();
                }
                game.draw(delta);
            }

            @Override
            public void onSurfaceChanged(javax.microedition.khronos.opengles.GL10 gl, int width, int height) {
                // Resize code here. For example, setting the viewport:
                gl.glViewport(0, 0, width, height);
            }
        });

        // attach the surface view to the context:
        // TODO: HERE

        log.debug("OpenGL ES Version: '{}'.", GLES20.glGetString(GLES20.GL_VERSION));

        this.state = State.INITIALIZED;

        return true;
    }

    @Override
    public void clear() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void setClearColor(Color color) {
        GLES20.glClearColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    @Override
    public byte[] readPixels(int x, int y, int width, int height, boolean opaque) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void dispose() {
        this.state = State.DISPOSED;
    }

    private void initGLESCapabilities() {
        // Disable face culling.
        GLES20.glDisable(GLES20.GL_CULL_FACE);

        // Disable depth testing.
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        // Enable blending for transparent pixels.
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        // Stencil test settings (if you use stencil operations).
        GLES20.glEnable(GLES20.GL_STENCIL_TEST);
    }
}
