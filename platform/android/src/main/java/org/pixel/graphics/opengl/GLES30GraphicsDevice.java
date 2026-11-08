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
import org.pixel.graphics.opengl.render.GLRenderBackend;
import org.pixel.graphics.render.DrawListSpriteBatch;
import org.pixel.graphics.render.SpriteBatch;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.pixel.graphics.GraphicsDevice;
import org.pixel.graphics.RenderStats;

public class GLES30GraphicsDevice extends GLSurfaceView implements GraphicsDevice {

    private final RenderStats stats = new RenderStats();

    private static final Logger log = LoggerFactory.getLogger(GLES30GraphicsDevice.class);

    private final AndroidGameHost host;
    private GLRenderBackend renderBackend;
    private final float[] clearColor = new float[4];

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
        // keep GPU resources when the activity pauses (devices may still drop the context; see the Android notes)
        this.setPreserveEGLContextOnPause(true);
        this.setRenderer(new GLSurfaceView.Renderer() {
            private final DeltaTime delta = new DeltaTime();

            @Override
            public void onSurfaceCreated(javax.microedition.khronos.opengles.GL10 gl, javax.microedition.khronos.egl.EGLConfig config) {
                initGLESCapabilities();
                setClearColor(host.settings().getBackgroundColor());

                log.debug("OpenGL ES: {0} ({1}).", GLES30.glGetString(GLES30.GL_VERSION),
                        GLES30.glGetString(GLES30.GL_RENDERER));
                if (renderBackend == null) { // URP v2 backend, shared with desktop through GLApi
                    renderBackend = new GLRenderBackend(AndroidGLApi.INSTANCE, stats);
                } else {
                    // called again = a new EGL context: the old one (and every GL object in it) is gone
                    renderBackend.contextRecreated();
                    log.error("The OpenGL ES context was recreated: the renderer was restored, but textures and "
                            + "font atlases loaded before are invalid until content reloading is implemented.");
                }

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
        if (renderBackend == null) {
            throw new IllegalStateException("The render backend is created with the GL surface (available from load()).");
        }
        return renderBackend;
    }

    @Override
    public SpriteBatch createSpriteBatch() {
        SpriteBatch spriteBatch = new DrawListSpriteBatch(getRenderBackend());
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
        // set every time: render passes clearing targets change the GL clear color
        GLES30.glClearColor(clearColor[0], clearColor[1], clearColor[2], clearColor[3]);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
    }

    @Override
    public void setClearColor(Color color) {
        clearColor[0] = color.getRed();
        clearColor[1] = color.getGreen();
        clearColor[2] = color.getBlue();
        clearColor[3] = color.getAlpha();
        GLES30.glClearColor(clearColor[0], clearColor[1], clearColor[2], clearColor[3]);
    }

    @Override
    public byte[] readPixels(int x, int y, int width, int height, boolean opaque) {
        // (x, y) in framebuffer pixels with the OpenGL origin (bottom-left); rows are returned bottom-up
        ByteBuffer pixels = ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.nativeOrder());
        GLES30.glPixelStorei(GLES30.GL_PACK_ALIGNMENT, 1);
        GLES30.glReadPixels(x, y, width, height, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, pixels);
        byte[] data = new byte[width * height * 4];
        pixels.get(data);
        if (opaque) {
            for (int i = 3; i < data.length; i += 4) {
                data[i] = (byte) 255;
            }
        }
        return data;
    }

    @Override
    public void dispose() {
        if (renderBackend != null) {
            renderBackend.dispose(); // GL thread (the host disposes from there)
            renderBackend = null;
        }
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
