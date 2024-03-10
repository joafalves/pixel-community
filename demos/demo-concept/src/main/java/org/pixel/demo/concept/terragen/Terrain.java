package org.pixel.demo.concept.terragen;

import org.pixel.commons.Color;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Terrain {
    private float minHeight;
    private Color color;
}
