package org.pixel.core;

import android.app.Activity;
import android.content.Context;
import android.view.MotionEvent;
import org.pixel.audio.AudioPlayer;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.content.ContentManager;
import org.pixel.content.MobileResourceLoader;
import org.pixel.content.importer.GLES30TextureImporter;
import org.pixel.content.importer.TextImporter;
import org.pixel.content.importer.TexturePackImporter;
import org.pixel.core.dev.DevTools;
import org.pixel.graphics.text.PrebakedSdfFontImporter;
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
    // touch state for the dev tools, written on the UI thread, read on the GL thread
    private volatile float touchX = -1, touchY = -1;
    private volatile boolean touchDown;
    private volatile boolean multiTouch; // three fingers went down: no pointer until all are lifted
    private boolean gestureCaptured; // the gesture started on the dev tools (UI thread only)
    // a touch began since the last frame (a quick tap may already be up again)
    private final java.util.concurrent.atomic.AtomicBoolean touchPressed = new java.util.concurrent.atomic.AtomicBoolean();

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
        boolean started = initContainer();
        if (started) {
            // keep the dev tools' physical size: one unit per density-independent pixel
            devTools.setScale(androidContext.getResources().getDisplayMetrics().density);
        }
        return started;
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
                new TexturePackImporter(),
                new PrebakedSdfFontImporter()); // fonts baked on desktop (SdfFontBaker)
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

    /**
     * Touches for the dev tools: call from the activity's {@code dispatchTouchEvent} (UI thread), before passing the
     * event on, and skip the game for events this returns {@code true} for:
     * <pre>
     * public boolean dispatchTouchEvent(MotionEvent event) {
     *     return host.onTouch(event) || super.dispatchTouchEvent(event);
     * }
     * </pre>
     * The first finger drives the dev tools' pointer; a three-finger tap shows or hides them. A gesture that starts on
     * the dev tools' window (or the three-finger tap) is theirs until all fingers are lifted; every other touch is
     * the game's. Without this call the dev tools still show (e.g. {@code setDevToolsVisible(true)}), but can't be
     * touched.
     *
     * @return Whether the event belongs to the dev tools.
     */
    public boolean onTouch(MotionEvent event) {
        if (devTools == null || !devTools.isAvailable()) {
            return false;
        }
        int action = event.getActionMasked();
        boolean ended = action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL;
        if (action == MotionEvent.ACTION_DOWN) {
            touchPressed.set(true);
            gestureCaptured = devTools.isOverUi(event.getX(0), event.getY(0));
        }
        if (action == MotionEvent.ACTION_POINTER_DOWN && event.getPointerCount() == 3) {
            devTools.requestToggle();
            multiTouch = true;
        }
        touchX = event.getX(0);
        touchY = event.getY(0);
        touchDown = !ended && !multiTouch;
        boolean ours = gestureCaptured || multiTouch;
        if (ended) {
            multiTouch = false;
            gestureCaptured = false;
        }
        return ours;
    }

    @Override
    protected void updateDevToolsInput(DevTools devTools) {
        boolean pressed = touchPressed.getAndSet(false); // a tap shorter than a frame still counts
        boolean down = (touchDown || pressed) && !multiTouch;
        if (down || pressed) {
            devTools.pointer(touchX, touchY, down);
        } else {
            devTools.pointer(-1f, -1f, false); // no finger: no pointer (nothing hovered, the game gets the next tap)
        }
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
