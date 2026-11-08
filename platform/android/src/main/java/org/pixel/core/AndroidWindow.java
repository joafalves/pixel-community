package org.pixel.core;

import android.app.Activity;

/**
 * {@link GameWindow} backed by the host's GL surface view.
 */
public final class AndroidWindow implements GameWindow {

    private final AndroidGameHost host;

    AndroidWindow(AndroidGameHost host) {
        this.host = host;
    }

    @Override
    public int getWidth() {
        return host.graphics().getWidth();
    }

    @Override
    public int getHeight() {
        return host.graphics().getHeight();
    }

    @Override
    public float getPixelRatio() {
        int virtualWidth = host.settings().getVirtualWidth();
        return virtualWidth > 0 ? getWidth() / (float) virtualWidth : 1f;
    }

    @Override
    public boolean isFocused() {
        return host.graphics().hasWindowFocus();
    }

    @Override
    public void setTitle(String title) {
        if (host.getAndroidContext() instanceof Activity) {
            final Activity activity = (Activity) host.getAndroidContext();
            activity.runOnUiThread(() -> activity.setTitle(title));
        }
    }
}
