package org.pixel.kotlin

import org.pixel.commons.DeltaTime
import org.pixel.content.ContentManager
import org.pixel.content.Texture
import org.pixel.core.Camera2D
import org.pixel.core.PixelWindow
import org.pixel.core.WindowSettings
import org.pixel.graphics.Color
import org.pixel.graphics.render.BlendMode
import org.pixel.graphics.render.SpriteBatch
import org.pixel.math.Vector2

class KotlinDemo(settings: WindowSettings?) : PixelWindow(settings) {
    private val gameCamera = Camera2D(this)
    private val content = ContentManager()

    private lateinit var spriteBatch: SpriteBatch
    private lateinit var spriteTexture: Texture

    override fun load() {
        backgroundColor = Color.BLACK
        spriteBatch = SpriteBatch()
        spriteTexture = content.load("images/earth-48x48.png", Texture::class.java)
    }

    override fun draw(delta: DeltaTime?) {
        spriteBatch.begin(gameCamera.viewMatrix, BlendMode.NORMAL_BLEND)
        spriteBatch.draw(spriteTexture, Vector2.ZERO, Color.WHITE, Vector2.HALF, 3f)
        spriteBatch.end()
    }

    override fun dispose() {
        spriteBatch.dispose()
        content.dispose()
        spriteTexture.dispose()
    }
}

fun main() {
    val settings = WindowSettings(800, 600)
    settings.setVsync(true)

    val window = KotlinDemo(settings)
    window.start()
}
