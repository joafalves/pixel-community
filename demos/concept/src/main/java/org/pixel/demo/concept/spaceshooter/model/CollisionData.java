package org.pixel.demo.concept.spaceshooter.model;

import lombok.Builder;
import lombok.Data;
import org.pixel.demo.concept.spaceshooter.entity.SpaceShipSprite;
import org.pixel.math.Vector2;

@Data
@Builder
public class CollisionData {
    private Vector2 position;
    private SpaceShipSprite target;
}
