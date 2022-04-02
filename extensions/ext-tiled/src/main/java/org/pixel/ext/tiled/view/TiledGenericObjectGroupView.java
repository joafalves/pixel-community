package org.pixel.ext.tiled.view;

import org.pixel.ext.tiled.content.*;

/**
 * A generic object group view.
 */
public interface TiledGenericObjectGroupView extends TiledView<TiledObjectGroup> {
    /**
     * Draws the given TiledTileObject.
     *
     * @param object The TiledTileObject to draw.
     * @param group  The TiledObjectGroup that contains the object.
     */
    default void draw(TiledTileObject object, TiledObjectGroup group) {

    }

    /**
     * Draws the given TiledRectangle.
     *
     * @param object The TiledRectangle to draw.
     * @param group  The TiledObjectGroup that contains the object.
     */
    default void draw(TiledRectangle object, TiledObjectGroup group) {

    }

    /**
     * Draws the given TiledEllipse.
     *
     * @param object The TiledEllipse to draw.
     * @param group  The TiledObjectGroup that contains the object.
     */
    default void draw(TiledEllipse object, TiledObjectGroup group) {

    }

    /**
     * Draws the given TiledPolygon.
     *
     * @param object The TiledPolygon to draw.
     * @param group  The TiledObjectGroup that contains the object.
     */
    default void draw(TiledPolygon object, TiledObjectGroup group) {

    }

    /**
     * Draws the given TiledPoint.
     *
     * @param object The TiledPoint to draw.
     * @param group  The TiledObjectGroup that contains the object.
     */
    default void draw(TiledPoint object, TiledObjectGroup group) {

    }
}
