package org.pixel.demo.android;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import org.pixel.commons.Color;
import org.pixel.core.AndroidGameHost;
import org.pixel.core.AndroidLauncher;
import org.pixel.core.MobileGameSettings;

/**
 * Runs {@link AndroidDemoGame} full screen.
 */
public class MainActivity extends Activity {

    private AndroidGameHost host;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        var metrics = getResources().getDisplayMetrics();
        var settings = new MobileGameSettings("Pixel Engine Demo", metrics.widthPixels, metrics.heightPixels);
        settings.setBackgroundColor(Color.CORNFLOWER_BLUE);
        settings.setDevMode(true);         // dev tools available: a three-finger tap shows or hides them
        settings.setDevToolsVisible(true); // shown from the start in this demo

        host = AndroidLauncher.create(new AndroidDemoGame(), settings, this);
        setContentView(host.graphics());

        WindowInsetsController insets = getWindow().getInsetsController();
        if (insets != null) {
            insets.hide(WindowInsets.Type.systemBars());
            insets.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return host.onTouch(event) || super.dispatchTouchEvent(event); // dev tools first, then the views
    }

    @Override
    protected void onResume() {
        super.onResume();
        host.graphics().onResume(); // GLSurfaceView: resume the render thread
    }

    @Override
    protected void onPause() {
        host.graphics().onPause(); // GLSurfaceView: pause the render thread
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // release the game and its GL resources on the GL thread (events still run while it is paused)
        host.graphics().queueEvent(host::dispose);
        super.onDestroy();
    }
}
