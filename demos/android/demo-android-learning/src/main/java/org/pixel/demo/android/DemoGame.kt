package org.pixel.demo.android

import android.content.Context
import org.pixel.commons.DeltaTime
import org.pixel.commons.ServiceProvider
import org.pixel.content.ContentManager
import org.pixel.core.Game
import org.pixel.core.MobileGameSettings
import org.pixel.graphics.shader.opengl.GLES30TextureArrayShader

class DemoGame(settings: MobileGameSettings?, context: Context?) : Game(settings, context) {

    override fun load() {
        val contentManager = ServiceProvider.create(ContentManager::class.java)


        val texture = contentManager.loadTexture("images/earth-48x48.png")

        val shader = GLES30TextureArrayShader()


        println(texture)

    }

    override fun update(delta: DeltaTime?) {

    }

    override fun draw(delta: DeltaTime?) {

    }
}