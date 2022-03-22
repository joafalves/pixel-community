package org.pixel.ext.ldtk;

import lombok.Builder;
import lombok.Getter;
import org.pixel.math.Vector2;

@Builder
@Getter
public class LdtkGameEntity {
    private String identifier;
    private Vector2 position;
    private Vector2 pivot;
    private int width;
    private int height;
}
