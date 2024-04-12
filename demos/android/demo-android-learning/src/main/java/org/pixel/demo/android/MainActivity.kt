package org.pixel.demo.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.pixel.commons.Color
import org.pixel.core.Game
import org.pixel.core.MobileGameSettings


class MainActivity : AppCompatActivity() {

    private val gameWindow: Game = AndroidDemoGame(
        MobileGameSettings(720, 1280).apply {
            title = "Pixel Engine Demo"
            backgroundColor = Color.CORNFLOWER_BLUE
        }, this
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (gameWindow.init()) {
            setContentView(gameWindow.graphicsDevice)
        }
    }
}

