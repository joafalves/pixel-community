package org.pixel.kotlin

import org.pixel.commons.DeltaTime
import org.pixel.core.PixelWindow
import org.pixel.core.WindowSettings

class KotlinDemo(settings: WindowSettings?) : PixelWindow(settings) {
    override fun load() {
        super.load()
    }

    override fun draw(delta: DeltaTime?) {
        super.draw(delta)
    }

    override fun dispose() {
        super.dispose()
    }
}

fun main() {
    val settings = WindowSettings(800, 600)
    settings.setVsync(true)

    val window = KotlinDemo(settings)
    window.start()
}
