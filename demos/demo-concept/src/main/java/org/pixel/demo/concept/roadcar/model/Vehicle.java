package org.pixel.demo.concept.roadcar.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import org.pixel.commons.Color;
import org.pixel.math.SizeInt;
import org.pixel.math.Vector2;

@Builder
@Getter
@Setter
public class Vehicle {
    private final SizeInt sizeInt;
    private final Vector2 position;
    private final Color color;
    private final float speed;

    private RoadSegment targetRoadSegment;
    private float orientation;
}
