package org.pixel.demo.learning.decs.components;

import lombok.Getter;
import org.pixel.content.Texture;
import org.pixel.ext.decs.Component;

/**
 * A component that holds the sprite texture of an entity.
 */
@Getter
public class SpriteComponent implements Component {
    private final Texture texture;

    public SpriteComponent(Texture texture) {
        this.texture = texture;
    }

}
