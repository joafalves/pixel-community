package org.pixel.demo.learning.decs.component;

import lombok.Getter;
import org.pixel.ext.decs.GameComponent;
import org.pixel.math.Vector2;

/**
 * A component that holds the position of an entity.
 */
@Getter
public class PositionGameComponent implements GameComponent {
    private final Vector2 position;

    public PositionGameComponent(float x, float y) {
        this.position = new Vector2(x, y);
    }
}
