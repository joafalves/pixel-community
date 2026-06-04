package org.pixel.demo.learning.blueprintadvanced.blueprint;

import org.pixel.blueprint.annotation.Auto;
import org.pixel.blueprint.annotation.Blueprint;
import org.pixel.blueprint.annotation.Component;
import org.pixel.commons.event.EventManager;
import org.pixel.commons.service.ServiceProvider;
import org.pixel.content.ContentManager;
import org.pixel.content.Texture;
import org.pixel.core.Camera2D;
import org.pixel.graphics.render.SpriteBatch;

@Blueprint
public class CoreBlueprint {

    private static final float WIDTH = 800f;
    private static final float HEIGHT = 600f;

    @Component
    public SpriteBatch spriteBatch() {
        return ServiceProvider.get(SpriteBatch.class);
    }

    @Component
    public Camera2D mainCamera() {
        return new Camera2D(WIDTH, HEIGHT);
    }

    @Component
    public EventManager eventManager() {
        return new EventManager();
    }

    @Component
    public ContentManager contentManager() {
        return ServiceProvider.get(ContentManager.class);
    }

    @Component(value = "starTexture", tags = {"texture", "layer:background"})
    public Texture starTexture(@Auto ContentManager contentManager) {
        return contentManager.load("images/green-32x32.png", Texture.class);
    }

    @Component(value = "orbRedTexture", tags = {"texture", "layer:foreground"})
    public Texture orbRedTexture(@Auto ContentManager contentManager) {
        return contentManager.load("images/red-32x32.png", Texture.class);
    }

    @Component(value = "orbBlueTexture", tags = {"texture", "layer:foreground"})
    public Texture orbBlueTexture(@Auto ContentManager contentManager) {
        return contentManager.load("images/blue-32x32.png", Texture.class);
    }

    @Component(value = "pulseTexture", tags = {"texture", "layer:special"})
    public Texture pulseTexture(@Auto ContentManager contentManager) {
        return contentManager.load("images/earth-48x48.png", Texture.class);
    }

    @Component(value = "gridTexture", tags = {"texture", "layer:debug"})
    public Texture gridTexture(@Auto ContentManager contentManager) {
        return contentManager.load("images/green-32x32.png", Texture.class);
    }
}
