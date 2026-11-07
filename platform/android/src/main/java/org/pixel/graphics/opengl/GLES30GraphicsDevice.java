package org.pixel.graphics.opengl;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.lifecycle.State;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.core.AndroidGameHost;
import org.pixel.graphics.backend.RenderBackend;
import org.pixel.graphics.render.GLES30SpriteBatch;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.graphics.GraphicsDevice;
import org.pixel.graphics.RenderStats;

public class GLES30GraphicsDevice extends GLSurfaceView implements GraphicsDevice {

    private final RenderStats stats = new RenderStats();

    private static final Logger log = LoggerFactory.getLogger(GLES30GraphicsDevice.class);

    private final AndroidGameHost host;

    private State state;

    public GLES30GraphicsDevice(AndroidGameHost host, Context context) {
        super(context);
        this.host = host;
        this.state = State.NEW;
    }

    @Override
    public boolean init() {
        if (this.state.hasInitialized()) {
            log.warn("Graphics device already initialized.");
            return false;
        }

        log.debug("Initializing OpenGL ES graphics device...");
        this.state = State.INITIALIZING;

        this.setEGLContextClientVersion(3); // Setting OpenGL ES 3.0 as the rendering context.
        this.setRenderer(new GLSurfaceView.Renderer() {
            private final DeltaTime delta = new DeltaTime();

            @Override
            public void onSurfaceCreated(javax.microedition.khronos.opengles.GL10 gl, javax.microedition.khronos.egl.EGLConfig config) {
                initGLESCapabilities();
                setClearColor(host.settings().getBackgroundColor());

                // Log the OpenGL ES version.
                log.debug("OpenGL ES Version: {0}.", GLES30.glGetString(GLES30.GL_VERSION));

                host.onSurfaceCreated(); // loads the game the first time
            }

            @Override
            public void onDrawFrame(javax.microedition.khronos.opengles.GL10 gl) {
                // android game loop (the host clears, updates, draws and ends the frame):
                delta.tick();
                host.onDrawFrame(delta);
            }

            @Override
            public void onSurfaceChanged(javax.microedition.khronos.opengles.GL10 gl, int width, int height) {
                log.debug("Surface changed: width={0}, height={1}.", width, height);

                // Resize code here. For example, setting the viewport:
                GLES30.glViewport(0, 0, width, height);

                // Notify the game about the viewport change.
                host.onSurfaceChanged(width, height);
            }
        });

        this.state = State.INITIALIZED;

        return true;
    }

    @Override
    public RenderBackend getRenderBackend() {
        throw new UnsupportedOperationException("The URP v2 render backend is not implemented for OpenGL ES yet.");
    }

    @Override
    public SpriteBatch createSpriteBatch() {
        GLES30SpriteBatch spriteBatch = new GLES30SpriteBatch(getContext());
        spriteBatch.init();
        return spriteBatch;
    }

    @Override
    public RenderStats getStats() {
        return stats;
    }

    @Override
    public void endFrame() {
        stats.endFrame();
    }

    @Override
    public void clear() {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
    }

    @Override
    public void setClearColor(Color color) {
        GLES30.glClearColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
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
        GLES30.glDisable(GLES30.GL_CULL_FACE);

        // Disable depth testing.
        GLES30.glDisable(GLES30.GL_DEPTH_TEST);

        // Enable blending for transparent pixels.
        GLES30.glEnable(GLES30.GL_BLEND);
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA);

        // Stencil test settings (if you use stencil operations).
        GLES30.glEnable(GLES30.GL_STENCIL_TEST);
    }
}
