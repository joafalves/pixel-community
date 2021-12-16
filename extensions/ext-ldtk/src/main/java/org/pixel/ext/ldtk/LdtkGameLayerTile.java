package org.pixel.ext.ldtk;

import lombok.Builder;
import lombok.Getter;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;

@Builder
@Getter
public class LdtkGameLayerTile {
    private final Vector2 position;
    private final Rectangle displayArea;
    private final Rectangle tilesetSource;
}
