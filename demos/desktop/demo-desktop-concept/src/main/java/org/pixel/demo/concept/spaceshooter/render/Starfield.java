package org.pixel.demo.concept.spaceshooter.render;

import org.pixel.commons.Color;
import org.pixel.demo.concept.spaceshooter.GameConfig;
import org.pixel.demo.concept.spaceshooter.assets.Assets;
import org.pixel.graphics.canvas.Canvas;

import java.util.Random;

/**
 * The scrolling background, in parallax: space tiles (slowest), drifting nebulae, then two layers of stars. A warp
 * factor stretches the stars into streaks (starting a run).
 */
public final class Starfield {

    private static final int TILE = 16;
    private static final int COLUMNS = GameConfig.WIDTH / TILE + 2, ROWS = GameConfig.HEIGHT / TILE + 1;
    private static final int TILE_COLUMNS = 64; // the tile pattern repeats every 64 columns
    private static final float TILE_SPEED = 6f;
    private static final int STARS = 140;

    private final Assets assets;
    private final byte[] tiles = new byte[TILE_COLUMNS * ROWS];
    private final float[] starX = new float[STARS], starY = new float[STARS], starSpeed = new float[STARS],
            starBright = new float[STARS], starPhase = new float[STARS];
    private final float[] nebulaX = {60f, 250f, 420f}, nebulaY = {60f, 160f, 90f}, nebulaR = {110f, 140f, 95f};
    private final Color scratch = new Color(), clear = new Color(0f, 0f, 0f, 0f);
    private float scroll, time, warp = 1f;

    public Starfield(Assets assets, Random random) {
        this.assets = assets;
        int total = 0;
        for (int w : assets.backdropWeights) {
            total += w;
        }
        for (int i = 0; i < tiles.length; i++) { // weighted: mostly plain space, a few stars and dust
            int pick = random.nextInt(total), tile = 0;
            while (pick >= assets.backdropWeights[tile]) {
                pick -= assets.backdropWeights[tile];
                tile++;
            }
            tiles[i] = (byte) tile;
        }
        for (int i = 0; i < STARS; i++) {
            boolean near = i % 3 == 0;
            starX[i] = random.nextFloat() * GameConfig.WIDTH;
            starY[i] = random.nextFloat() * GameConfig.HEIGHT;
            starSpeed[i] = near ? 55f + random.nextFloat() * 35f : 14f + random.nextFloat() * 18f;
            starBright[i] = near ? 0.7f + random.nextFloat() * 0.3f : 0.25f + random.nextFloat() * 0.35f;
            starPhase[i] = random.nextFloat() * 6.28f;
        }
    }

    /** Speed multiplier for the stars (1 = cruising; higher stretches them into streaks). */
    public void setWarp(float warp) {
        this.warp = Math.max(0f, warp);
    }

    public float warp() {
        return warp;
    }

    public void update(float dt) {
        time += dt;
        scroll += TILE_SPEED * dt * (0.5f + 0.5f * warp);
        for (int i = 0; i < STARS; i++) {
            starX[i] -= starSpeed[i] * warp * dt;
            if (starX[i] < -20f) {
                starX[i] += GameConfig.WIDTH + 40f;
            }
        }
        for (int i = 0; i < nebulaX.length; i++) {
            nebulaX[i] -= 3f * warp * dt;
            if (nebulaX[i] < -nebulaR[i]) {
                nebulaX[i] += GameConfig.WIDTH + nebulaR[i] * 2f;
            }
        }
    }

    /**
     * @param backdrop The tile layer (plain images).
     * @param stars    The additive layer.
     */
    public void draw(Canvas backdrop, Canvas stars) {
        int firstColumn = (int) (scroll / TILE);
        float offset = scroll - firstColumn * TILE;
        for (int c = 0; c < COLUMNS; c++) {
            int column = (firstColumn + c) % TILE_COLUMNS;
            float x = Math.round(c * TILE - offset);
            for (int r = 0; r < ROWS; r++) {
                var region = assets.backdropTiles[tiles[column * ROWS + r]];
                backdrop.image(assets.sprites, x, r * TILE).source(region);
            }
        }
        for (int i = 0; i < nebulaX.length; i++) {
            scratch.set(i % 2 == 0 ? Palette.NEBULA_VIOLET : Palette.NEBULA_TEAL);
            scratch.setAlpha(0.16f);
            stars.circle(nebulaX[i], nebulaY[i], nebulaR[i]).fillRadial(nebulaX[i], nebulaY[i], nebulaR[i], scratch,
                    clear);
        }
        for (int i = 0; i < STARS; i++) {
            float twinkle = 0.75f + 0.25f * (float) Math.sin(time * 3f + starPhase[i]);
            float b = starBright[i] * twinkle;
            scratch.set(0.75f + 0.25f * b, 0.8f + 0.2f * b, 1f, b);
            float x = Math.round(starX[i]), y = Math.round(starY[i]);
            float streak = starSpeed[i] * (warp - 1f) * 0.06f;
            if (streak > 1f) {
                stars.line(x, y + 0.5f, x + streak, y + 0.5f).width(1f).color(scratch);
            } else {
                float size = starSpeed[i] > 50f ? 2f : 1f;
                stars.rect(x, y, size, size).fill(scratch);
            }
        }
    }
}
