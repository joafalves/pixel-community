package org.pixel.ext.tiled.view;

import org.pixel.ext.tiled.content.*;

public interface TiledGenericObjectGroupView extends TiledView<TiledObjectGroup> {
    default void draw(TiledTileObject object, TiledObjectGroup group) {

    }

    default void draw(TiledRectangle object, TiledObjectGroup group) {

    }

    default void draw(TiledEllipse object, TiledObjectGroup group) {

    }

    default void draw(TiledPolygon object, TiledObjectGroup group) {

    }

    default void draw(TiledPoint object, TiledObjectGroup group) {

    }
}
