package org.pixel.demo.learning.decs.component;

import lombok.Getter;
import org.pixel.ext.decs.GameComponent;
import org.pixel.math.Rectangle;

/**
 * A component that holds the collision box of an entity.
 */
@Getter
public class CollisionGameComponent implements GameComponent {
    private final Rectangle boundingBox;

    public CollisionGameComponent(float x, float y, float width, float height) {
        this.boundingBox = new Rectangle(x, y, width, height);
    }

}
