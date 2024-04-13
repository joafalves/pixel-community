package org.pixel.demo.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.pixel.commons.Color
import org.pixel.core.Game
import org.pixel.core.MobileGameSettings


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        window.decorView.windowInsetsController?.hide(android.view.WindowInsets.Type.navigationBars())

        val gameWindow: Game = AndroidDemoGame(
            MobileGameSettings(resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels).apply {
                title = "Pixel Engine Demo"
                backgroundColor = Color.CORNFLOWER_BLUE
            }, this
        )

        if (gameWindow.init()) {
            setContentView(gameWindow.graphicsDevice)
        }
    }
}

