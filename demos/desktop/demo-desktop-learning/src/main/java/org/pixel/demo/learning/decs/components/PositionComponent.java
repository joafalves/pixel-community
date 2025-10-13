package org.pixel.demo.learning.decs.components;

import lombok.Getter;
import org.pixel.ext.decs.Component;
import org.pixel.math.Vector2;

/**
 * A component that holds the position of an entity.
 */
@Getter
public class PositionComponent implements Component {
    private final Vector2 position;

    public PositionComponent(float x, float y) {
        this.position = new Vector2(x, y);
    }
}
