package org.pixel.demo.concept.physics;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PhysicsBody {
    private double mass; // Mass of the body in kilograms
    private Vector2D position; // Position of the body in the world
    private Vector2D velocity; // Velocity of the body in the world
}
