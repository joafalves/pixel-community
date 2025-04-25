package org.pixel.demo.concept.bluegem.blueprint;

import org.pixel.blueprint.annotation.Blueprint;
import org.pixel.blueprint.annotation.Component;
import org.pixel.commons.ServiceProvider;
import org.pixel.content.ContentManager;

@Blueprint
public class GameSceneBlueprint {

    @Component("game.contentManager")
    public ContentManager contentManager() {
        return ServiceProvider.get(ContentManager.class);
    }
}
