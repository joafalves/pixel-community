package org.pixel.demo.learning.decs.components;

import lombok.Getter;
import org.pixel.ext.decs.Component;
import org.pixel.math.Vector2;

/**
 * A component that holds the velocity of an entity.
 */
@Getter
public class VelocityComponent implements Component {
    private final Vector2 velocity;

    public VelocityComponent() {
        this.velocity = new Vector2(0, 0);
    }

}
