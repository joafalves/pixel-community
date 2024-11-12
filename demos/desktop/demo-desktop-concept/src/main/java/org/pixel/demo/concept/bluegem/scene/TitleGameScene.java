package org.pixel.demo.concept.bluegem.scene;

import org.pixel.content.ContentManager;
import org.pixel.core.Camera2D;
import org.pixel.ext.ecs.GameScene;
import org.pixel.ext.ecs.Sprite;
import org.pixel.graphics.render.SpriteBatch;

public class TitleGameScene extends GameScene {

    private final ContentManager contentManager;

    public TitleGameScene(String name, Camera2D gameCamera, SpriteBatch spriteBatch, ContentManager contentManager) {
        super(name, gameCamera, spriteBatch);
        this.contentManager = contentManager;
    }

    @Override
    public void load() {
        var sprite = new Sprite("Gem", contentManager.loadTexture("images/gem_1024x1024.png"));

        addChild(sprite);
    }

    @Override
    public void dispose() {
        contentManager.dispose(); // This will dispose all content-files loaded by the content-manager
        super.dispose();
    }
}
