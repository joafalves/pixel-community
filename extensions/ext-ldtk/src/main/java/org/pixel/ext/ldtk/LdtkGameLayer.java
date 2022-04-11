package org.pixel.ext.ldtk;

import lombok.Getter;
import lombok.Setter;
import org.pixel.commons.DeltaTime;
import org.pixel.content.Texture;
import org.pixel.graphics.Color;
import org.pixel.graphics.SpriteDrawable;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Vector2;

import java.util.List;

@Setter
@Getter
public class LdtkGameLayer implements SpriteDrawable {

    private Boolean visible;
    private int gridSize;
    private int gridWidth;
    private int gridHeight;
    private String identifier;
    private Texture tilesetTexture;
    private List<LdtkGameLayerTile> tiles;

    @Override
    public void draw(DeltaTime delta, SpriteBatch spriteBatch) {
        if (!visible) {
            return;
        }

        if (tiles != null) {
            for (LdtkGameLayerTile tile : tiles) {
                spriteBatch.draw(tilesetTexture, tile.getDisplayArea(), tile.getTilesetSource(), Color.WHITE,
                        Vector2.ZERO, 0f);
            }
        }
    }
}
