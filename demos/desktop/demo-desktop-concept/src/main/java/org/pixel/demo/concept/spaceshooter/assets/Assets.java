package org.pixel.demo.concept.spaceshooter.assets;

import org.pixel.commons.lifecycle.Disposable;
import org.pixel.content.ContentManager;
import org.pixel.content.Texture;
import org.pixel.content.TexturePack;
import org.pixel.content.TextureFrame;
import org.pixel.graphics.material.Material;
import org.pixel.graphics.text.SdfFont;
import org.pixel.graphics.text.SdfFontSettings;
import org.pixel.math.Rectangle;

/**
 * Everything the game loads, in one place. Asset paths follow the folders under {@code resources/spaceshooter/}:
 * {@code sprites/} (the sprite map), {@code fx/} (animation sheets), {@code fonts/} and {@code shaders/}.
 * <p>
 * The textures and the font belong to the content manager (released with it); the material is owned here.
 */
public final class Assets implements Disposable {

    public static final String ROOT = "spaceshooter/";
    public static final String HIT_FLASH_SHADER = "shaders/hit_flash.glsl";
    public static final String SHOCKWAVE_SHADER = "shaders/shockwave.glsl";

    // sprite map (sprites/spritemap.json): all art faces right
    public final Texture sprites;
    public final Rectangle player, raider, dart, playerShot, enemyShot;
    public final Rectangle[] backdropTiles;
    public final int[] backdropWeights;

    /** Engine flame (25x40 cells, pointing down): ignition, then the loop. */
    public final Sheet flameIgnite, flameLoop;
    /** A steadier flame for enemy engines. */
    public final Sheet flameSteady;
    /** Ships breaking apart (64x64 cells, frame 0 is the intact ship, so the animation starts at 1). */
    public final Sheet raiderBreakup, dartBreakup;
    /** A fireball (100x100 cells). */
    public final Sheet fireball;

    public final SdfFont font;

    /** Hit flash (effect material): every ship draws with it, the command's data x being the flash. */
    public final Material hitFlash;
    /** The shockwave post effect's source (see {@code fx.Shockwave}). */
    public final String shockwaveSource;

    public Assets(ContentManager content) {
        TexturePack pack = content.loadTexturePack(ROOT + "sprites/spritemap.json");
        sprites = pack.getTexture();
        player = region(pack, "player-ship");
        raider = region(pack, "enemy-ship1");
        dart = region(pack, "enemy-ship2");
        playerShot = region(pack, "bullet1");
        enemyShot = region(pack, "bullet2");
        String[] tiles = {"bg-01", "bg-02", "bg-03", "bg-04"};
        backdropTiles = new Rectangle[tiles.length];
        backdropWeights = new int[tiles.length];
        for (int i = 0; i < tiles.length; i++) {
            TextureFrame frame = pack.getFrame(tiles[i]);
            backdropTiles[i] = frame.getSource();
            Number weight = frame.getAttributes() != null ? frame.getAttributes().get("weight", Number.class) : null;
            backdropWeights[i] = weight != null ? Math.max(1, weight.intValue()) : 1;
        }

        Texture flame = texture(content, "fx/engine_flame.png");
        flameIgnite = new Sheet(flame, 25, 40, 9, 29);
        flameLoop = new Sheet(flame, 25, 40, 30, 119);
        flameSteady = new Sheet(texture(content, "fx/engine_flame_steady.png"), 25, 40, 0, 44);
        raiderBreakup = new Sheet(texture(content, "fx/raider_breakup.png"), 64, 64, 1, 16);
        dartBreakup = new Sheet(texture(content, "fx/dart_breakup.png"), 64, 64, 1, 16);
        fireball = new Sheet(texture(content, "fx/fireball.png"), 100, 100, 14, 72);

        font = content.load(ROOT + "fonts/8-bit-pusab.ttf", SdfFont.class,
                SdfFontSettings.builder().characters(SdfFontSettings.ASCII).build());

        hitFlash = Material.effect(text(content, HIT_FLASH_SHADER)).setName("hit flash");
        shockwaveSource = text(content, SHOCKWAVE_SHADER);
    }

    private static Rectangle region(TexturePack pack, String name) {
        TextureFrame frame = pack.getFrame(name);
        if (frame == null) {
            throw new IllegalStateException("The sprite map has no frame \"" + name + "\".");
        }
        return frame.getSource();
    }

    private static Texture texture(ContentManager content, String path) {
        Texture texture = content.loadTexture(ROOT + path);
        if (texture == null) {
            throw new IllegalStateException("Unable to load " + ROOT + path);
        }
        return texture;
    }

    private static String text(ContentManager content, String path) {
        String text = content.load(ROOT + path, String.class);
        if (text == null) {
            throw new IllegalStateException("Unable to load " + ROOT + path);
        }
        return text;
    }

    @Override
    public void dispose() {
        hitFlash.dispose();
    }
}
