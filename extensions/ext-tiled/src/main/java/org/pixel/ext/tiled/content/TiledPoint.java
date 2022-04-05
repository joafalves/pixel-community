package org.pixel.ext.tiled.content;

import org.pixel.ext.tiled.view.TiledGenericObjectGroupView;

/**
 * A point TiledObject.
 */
public class TiledPoint extends TiledObject {
    @Override
    public void draw(TiledObjectGroup group, TiledGenericObjectGroupView view) {
        view.draw(this, group);
    }
}
