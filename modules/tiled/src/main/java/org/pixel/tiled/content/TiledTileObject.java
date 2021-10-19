package org.pixel.tiled.content;

import org.pixel.graphics.render.SpriteBatch;
import org.pixel.tiled.view.TiledObjectGroupView;

public class TiledTileObject extends TiledObject {
    private long gID;

    public TiledTileObject(TileMap tileMap) {
        super(tileMap);
    }

    public long getgID() {
        return gID;
    }

    public void setgID(long gID) {
        this.gID = gID;
    }

    @Override
    public void draw(SpriteBatch spriteBatch, TiledObjectGroup group, TiledObjectGroupView view) {
        view.draw(spriteBatch, this, group);
    }
}
