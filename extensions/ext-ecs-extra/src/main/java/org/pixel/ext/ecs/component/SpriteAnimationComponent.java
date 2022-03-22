package org.pixel.ext.ecs.component;

import org.pixel.commons.DeltaTime;
import org.pixel.ext.ecs.GameComponent;
import org.pixel.ext.ecs.GameObject;
import org.pixel.ext.ecs.Sprite;

public class SpriteAnimationComponent extends GameComponent {

    private float delay;
    private int preFrame;
    private int startFrame;
    private int endFrame;
    private int rows;
    private int columns;
    private boolean loop;

    protected boolean isPlaying;
    protected int frame;
    protected float elapsed = 0;

    /**
     * Constructor.
     *
     * @param rows    The number of rows in the sprite sheet.
     * @param columns The number of columns in the sprite sheet.
     * @param delay   The delay between frames in milliseconds.
     */
    public SpriteAnimationComponent(int rows, int columns, float delay) {
        this.delay = delay;
        this.preFrame = -1;
        this.startFrame = 0;
        this.endFrame = rows * columns - 1;
        this.isPlaying = false;
        this.frame = 0;
        this.setRows(rows);
        this.setColumns(columns);
    }

    @Override
    public void attached(GameObject parent, GameObject previousParent) {
        super.attached(parent, previousParent);
        if (!(parent instanceof Sprite)) {
            throw new RuntimeException("SpriteAnimationComponent can only be attached to a Sprite");
        }
    }

    @Override
    public void update(DeltaTime delta) {
        super.update(delta);
        if (isPlaying) {
            animate(delta);
        }
    }

    private void animate(DeltaTime delta) {
        elapsed += delta.getElapsedMs();
        if (elapsed >= delay) {
            elapsed = 0;
            frame++;
            if (frame > endFrame) {
                if (loop) {
                    frame = startFrame;
                } else {
                    frame = endFrame;
                    isPlaying = false;
                }
            }
            setFrame();
        }
    }

    protected void setFrame() {
        var sprite = (Sprite) getGameObject();
        var width = sprite.getTexture().getWidth() / columns;
        var height = sprite.getTexture().getHeight() / rows;
        int x = (int) (frame % columns * width);
        int y = (int) (frame / columns * height);

        sprite.getTextureSource().set(x, y, width, height);
    }

    /**
     * Play the animation.
     *
     * @param loop Whether the animation should loop.
     */
    public void play(boolean loop) {
        this.setLoop(loop);
        this.play();
    }

    /**
     * Play the animation.
     */
    public void play() {
        setFrame();
        isPlaying = true;
        elapsed = 0;
    }

    /**
     * Pause the animation.
     */
    public void pause() {
        isPlaying = false;
    }

    /**
     * Stop the animation.
     */
    public void stop() {
        isPlaying = false;
        frame = preFrame >= 0 ? preFrame : startFrame;
    }

    /**
     * Restarts the animation from the beginning.
     */
    public void restart() {
        this.stop();
        this.play();
    }

    /**
     * Get the delay between frames in seconds.
     *
     * @return The delay between frames in seconds.
     */
    public float getDelay() {
        return delay;
    }

    /**
     * Set the delay between frames in seconds.
     *
     * @param delay The delay between frames in seconds.
     */
    public void setDelay(float delay) {
        this.delay = delay;
    }

    /**
     * Get the number of rows in the sprite sheet.
     *
     * @return The number of rows in the sprite sheet.
     */
    public int getRows() {
        return rows;
    }

    /**
     * Set the number of rows in the sprite sheet.
     *
     * @param rows The number of rows in the sprite sheet.
     */
    public void setRows(int rows) {
        this.rows = rows;
    }

    /**
     * Get the number of columns in the sprite sheet.
     *
     * @return The number of columns in the sprite sheet.
     */
    public int getColumns() {
        return columns;
    }

    /**
     * Set the number of columns in the sprite sheet.
     *
     * @param columns The number of columns in the sprite sheet.
     */
    public void setColumns(int columns) {
        this.columns = columns;
    }

    /**
     * Get whether the animation should loop.
     *
     * @return Whether the animation should loop.
     */
    public boolean isLoop() {
        return loop;
    }

    /**
     * Set whether the animation should loop.
     *
     * @param loop Whether the animation should loop.
     */
    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    /**
     * Get the start frame of the animation.
     *
     * @return The start frame of the animation.
     */
    public int getStartFrame() {
        return startFrame;
    }

    /*+
     * Set the start frame of the animation.
     *
     * @param startFrame The start frame of the animation.
     */
    public void setStartFrame(int startFrame) {
        this.startFrame = startFrame;
    }

    /**
     * Get the end frame of the animation.
     */
    public int getEndFrame() {
        return endFrame;
    }

    /**
     * Set the end frame of the animation.
     *
     * @param endFrame The end frame of the animation.
     */
    public void setEndFrame(int endFrame) {
        this.endFrame = endFrame;
    }

    /**
     * Get the pre-frame of the animation.
     *
     * @return The pre-frame of the animation.
     */
    public int getPreFrame() {
        return preFrame;
    }

    /**
     * Set the pre-frame of the animation. Use this if the animation has a pre-phase before the animation loop (the
     * pre-phase only plays when the animation is stopped or restarted).
     *
     * @param preFrame The pre-frame of the animation (-1 for no pre-frame).
     */
    public void setPreFrame(int preFrame) {
        this.preFrame = preFrame;
    }
}
