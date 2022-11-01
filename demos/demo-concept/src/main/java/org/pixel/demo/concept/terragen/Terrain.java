package org.pixel.demo.concept.terragen;

import lombok.Builder;
import lombok.Getter;
import org.pixel.graphics.Color;

@Getter
@Builder
public class Terrain {
    private float minHeight;
    private Color color;
}
