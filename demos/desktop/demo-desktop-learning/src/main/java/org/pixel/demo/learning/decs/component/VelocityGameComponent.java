package org.pixel.demo.learning.decs.component;

import lombok.Getter;
import org.pixel.ext.decs.GameComponent;
import org.pixel.math.Vector2;

/**
 * A component that holds the velocity of an entity.
 */
@Getter
public class VelocityGameComponent implements GameComponent {
    private final Vector2 velocity;

    public VelocityGameComponent() {
        this.velocity = new Vector2(0, 0);
    }

}
