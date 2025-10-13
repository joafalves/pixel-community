package org.pixel.demo.learning.decs.components;

import lombok.Getter;
import org.pixel.ext.decs.Component;
import org.pixel.math.Rectangle;

/**
 * A component that holds the collision box of an entity.
 */
@Getter
public class CollisionComponent implements Component {
    private final Rectangle boundingBox;

    public CollisionComponent(float x, float y, float width, float height) {
        this.boundingBox = new Rectangle(x, y, width, height);
    }

}
