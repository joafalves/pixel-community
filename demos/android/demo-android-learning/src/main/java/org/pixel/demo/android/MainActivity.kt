package org.pixel.demo.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.pixel.commons.Color
import org.pixel.core.Game
import org.pixel.core.MobileGameSettings


class MainActivity : AppCompatActivity() {

    private val gameWindow: Game

    init {
        val settings = MobileGameSettings(300, 600)
        settings.backgroundColor = Color.CORNFLOWER_BLUE

        gameWindow = DemoGame(settings, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settings = MobileGameSettings(300, 600)
        settings.backgroundColor = Color.CORNFLOWER_BLUE

        if (gameWindow.init()) {
            setContentView(gameWindow.graphicsDevice)
        }
    }
}

