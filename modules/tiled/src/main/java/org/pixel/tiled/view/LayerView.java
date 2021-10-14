package org.pixel.tiled.view;

import org.pixel.graphics.Color;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;
import org.pixel.tiled.content.Layer;
import org.pixel.tiled.content.TileMap;
import org.pixel.tiled.content.TileSet;

import java.util.List;
import java.util.ListIterator;

public class LayerView implements TiledViewer<Layer> {
    private static final long HORIZONTAL_FLIP_FLAG = 0x80000000;
    private static final long VERTICAL_FLIP_FLAG = 0x40000000;
    private static final long DIAGONAL_FLIP_FLAG = 0x20000000;

    @Override
    public void draw(SpriteBatch spriteBatch, Layer layer) {
        List<TileSet> tileSets = layer.getTileMap().getTileSets();

        for(int y = 0; y < layer.getHeight(); y++) {
            for(int x = 0; x < layer.getWidth(); x++) {
                long gID = layer.getTiles()[y][x];
                ListIterator<TileSet> itr = tileSets.listIterator(tileSets.size());

                gID &= ~(HORIZONTAL_FLIP_FLAG | VERTICAL_FLIP_FLAG | DIAGONAL_FLIP_FLAG);

                if(gID == 0) continue;

                while(itr.hasPrevious()) {
                    TileSet tileSet = itr.previous();

                    if(tileSet.getFirstGId() <= gID) {
                        Rectangle source = tileSet.sourceAt(gID - tileSet.getFirstGId());

                        Vector2 position = new Vector2(x * tileSet.getTileWidth(), y * tileSet.getTileHeight());

                        spriteBatch.draw(tileSet.getTexture(), position, source, Color.WHITE, Vector2.HALF, 1f, 0f);
                        break;
                    }
                }
            }
        }
    }
}
