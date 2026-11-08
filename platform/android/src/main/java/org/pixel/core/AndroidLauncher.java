package org.pixel.core;

import android.content.Context;

/**
 * Runs a {@link GameApp} on Android (OpenGL ES 3.0).
 *
 * <pre>
 * AndroidGameHost host = AndroidLauncher.create(new MyGame(), settings, this);
 * setContentView(host.graphics());   // the GLSurfaceView
 * </pre>
 */
public final class AndroidLauncher {

    private AndroidLauncher() {
    }

    /**
     * Create the host and its platform services. The game's {@code load()} runs on the GL thread once the surface
     * view is shown.
     *
     * @param app      The game.
     * @param settings The settings.
     * @param context  The Android context (an Activity, so that {@code exit()} can finish it).
     * @return The host; show {@code host.graphics()} (a GLSurfaceView) as the content view.
     * @throws IllegalStateException If the platform could not be initialized.
     */
    public static AndroidGameHost create(GameApp app, MobileGameSettings settings, Context context) {
        AndroidGameHost host = new AndroidGameHost(app, settings, context);
        if (!host.start()) {
            throw new IllegalStateException("Unable to initialize the Android game host.");
        }
        return host;
    }
}
