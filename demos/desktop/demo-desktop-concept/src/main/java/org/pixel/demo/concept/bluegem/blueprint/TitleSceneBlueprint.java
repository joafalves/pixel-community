package org.pixel.demo.concept.bluegem.blueprint;

import org.pixel.blueprint.annotation.Auto;
import org.pixel.blueprint.annotation.Blueprint;
import org.pixel.blueprint.annotation.Component;
import org.pixel.commons.ServiceProvider;
import org.pixel.content.ContentManager;
import org.pixel.core.Camera2D;
import org.pixel.demo.concept.bluegem.scene.TitleGameScene;
import org.pixel.graphics.render.SpriteBatch;

@Blueprint
public class TitleSceneBlueprint {

    @Component("title.contentManager")
    public ContentManager contentManager() {
        return ServiceProvider.get(ContentManager.class);
    }

    @Component("title.scene")
    public TitleGameScene titleGameScene(
            @Auto Camera2D mainCamera,
            @Auto SpriteBatch spriteBatch,
            @Auto("title.contentManager") ContentManager contentManager) {

        return new TitleGameScene("TitleScene", mainCamera, spriteBatch, contentManager);
    }

}
