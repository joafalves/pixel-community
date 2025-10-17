package org.pixel.demo.learning.decs.component;

import lombok.Getter;
import org.pixel.content.Texture;
import org.pixel.ext.decs.GameComponent;

/**
 * A component that holds the sprite texture of an entity.
 */
@Getter
public class SpriteGameComponent implements GameComponent {
    private final Texture texture;

    public SpriteGameComponent(Texture texture) {
        this.texture = texture;
    }

}
