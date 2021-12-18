package org.pixel.ext.ldtk;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.pixel.commons.DeltaTime;
import org.pixel.content.Texture;
import org.pixel.graphics.Color;
import org.pixel.graphics.SpriteDrawable;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Vector2;

@Builder
@Getter
public class LdtkGameLayer implements SpriteDrawable {

    private Boolean visible;
    private String identifier;

    private Texture tilesetTexture;
    private List<LdtkGameLayerTile> tileList;

    @Override
    public void draw(DeltaTime delta, SpriteBatch spriteBatch) {
        if (!visible) {
            return;
        }

        if (tileList != null) {
            for (LdtkGameLayerTile tile : tileList) {
                spriteBatch.draw(tilesetTexture, tile.getDisplayArea(), tile.getTilesetSource(), Color.WHITE,
                        Vector2.ZERO, 0f);
            }
        }
    }
}