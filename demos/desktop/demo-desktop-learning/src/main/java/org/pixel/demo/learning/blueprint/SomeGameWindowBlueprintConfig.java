package org.pixel.demo.learning.blueprint;

import org.pixel.blueprint.annotation.Auto;
import org.pixel.blueprint.annotation.Blueprint;
import org.pixel.blueprint.annotation.Component;
import org.pixel.content.ContentManager;
import org.pixel.content.Texture;

@Blueprint
public class SomeGameWindowBlueprintConfig {
    @Component(value = "someGameWindowContentManager")
    public ContentManager contentManager() {
        // Tip: You could have a different blueprint for each game window in your game...
        return ContentManager.create();
    }

    @Component
    public Texture backgroundTexture(@Auto ContentManager someGameWindowContentManager) {
        return someGameWindowContentManager.loadTexture("images/screenshot-600x320.png");
    }
}
