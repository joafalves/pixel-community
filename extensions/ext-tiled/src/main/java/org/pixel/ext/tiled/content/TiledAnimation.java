package org.pixel.ext.tiled.content;

import java.util.List;

/**
 * An animation for a TiledTile of a TiledTileSet. Uses an ordered list of TiledFrames to store animation frames.
 */
public class TiledAnimation {
    List<TiledFrame> frameList;
    private int totalMsCount;

    public TiledAnimation() {
        this.totalMsCount = 0;
    }

    public List<TiledFrame> getFrameList() {
        return frameList;
    }

    /**
     * Sets the list of frames for this animation. Frames must be ordered by order of appearance.
     *
     * @param frameList the list of ordered frames.
     */
    public void setFrameList(List<TiledFrame> frameList) {
        this.frameList = frameList;
        this.totalMsCount = 0;

        for (TiledFrame frame : frameList) {
            totalMsCount += frame.getDuration();
        }
    }

    /**
     * @return the total number of milliseconds for a full animation cycle.
     */
    public int getTotalMsCount() {
        return totalMsCount;
    }

    /**
     * Gets the gID of the current animation frame given the number of milliseconds that have passed since the 
     * animation has started.
     *
     * @param currentMs the number of milliseconds that have passed since the animation has started
     * @param defaultGID the gID to be returned if the animation has no frames.
     * @return the gID of the current animation frame
     */
    public long getCurrentGID(long currentMs, long defaultGID) {
        long currentFrame = currentMs % this.totalMsCount;
        currentFrame = Math.abs(currentFrame);
        long frameSum = 0;

        for (TiledFrame animFrame : this.frameList) {
            frameSum += animFrame.getDuration();

            if (frameSum > currentFrame) {
                return animFrame.getLocalId();
            }
        }

        return defaultGID;
    }
}
