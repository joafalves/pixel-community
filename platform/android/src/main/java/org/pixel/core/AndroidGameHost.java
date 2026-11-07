package org.pixel.core;

import android.app.Activity;
import android.content.Context;
import org.pixel.audio.AudioPlayer;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.content.ContentManager;
import org.pixel.content.MobileResourceLoader;
import org.pixel.content.importer.GLES30TextureImporter;
import org.pixel.content.importer.TextImporter;
import org.pixel.content.importer.TexturePackImporter;
import org.pixel.graphics.GraphicsBackend;
import org.pixel.graphics.opengl.GLES30GraphicsDevice;

/**
 * Android host (GLSurfaceView, OpenGL ES 3.0) running a {@link GameApp}. Created by {@link AndroidLauncher}.
 * <p>
 * The GL thread drives the game through {@link #onSurfaceCreated()}, {@link #onDrawFrame(DeltaTime)} and
 * {@link #onSurfaceChanged(int, int)}.
 */
public final class AndroidGameHost extends GameContainer<GLES30GraphicsDevice, MobileGameSettings> {

    private static final Logger log = LoggerFactory.getLogger(AndroidGameHost.class);

    private final Context androidContext;
    private final AndroidWindow window;
    private boolean loaded;

    AndroidGameHost(GameApp app, MobileGameSettings settings, Context androidContext) {
        super(app, settings);
        this.androidContext = androidContext;
        this.window = new AndroidWindow(this);
    }

    /**
     * Initialize the platform services (the game is loaded once the GL surface exists).
     *
     * @return True on success.
     */
    boolean start() {
        return initContainer();
    }

    @Override
    protected boolean initGraphicsDevice() {
        log.debug("Initializing {0} graphics device.", settings.getGraphicsBackend());
        if (settings.getGraphicsBackend() != GraphicsBackend.OpenGL) {
            throw new UnsupportedOperationException("Unsupported graphics backend: " + settings.getGraphicsBackend());
        }
        this.graphicsDevice = new GLES30GraphicsDevice(this, androidContext);
        return graphicsDevice.init();
    }

    @Override
    protected boolean initAudio() {
        return true; // no Android audio backend yet (see audio())
    }

    @Override
    protected ContentManager createContentManager() {
        return new ContentManager(new MobileResourceLoader(androidContext),
                new GLES30TextureImporter(),
                new TextImporter(),
                new TexturePackImporter());
    }

    @Override
    protected AudioPlayer createAudioPlayer() {
        return null;
    }

    @Override
    public AudioPlayer audio() {
        throw new UnsupportedOperationException("Audio is not implemented on Android yet.");
    }

    @Override
    public AndroidWindow window() {
        return window;
    }

    @Override
    public void exit() {
        if (androidContext instanceof Activity) {
            final Activity activity = (Activity) androidContext;
            activity.runOnUiThread(activity::finish);
        }
    }

    /**
     * Release the game and the platform services. Call from the GL thread (e.g. {@code queueEvent}) when the
     * activity is destroyed.
     */
    public void dispose() {
        disposeContainer();
    }

    // region GL thread callbacks (called by GLES30GraphicsDevice)

    public void onSurfaceCreated() {
        if (!loaded) {
            loaded = true;
            loadApp();
        }
    }

    public void onDrawFrame(DeltaTime delta) {
        updateFrame(delta);
        drawFrame(delta);
    }

    public void onSurfaceChanged(int width, int height) {
        onViewportChanged(width, height);
    }

    // endregion

    Context getAndroidContext() {
        return androidContext;
    }
}
