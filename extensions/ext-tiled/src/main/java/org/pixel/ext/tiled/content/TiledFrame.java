package org.pixel.ext.tiled.content;

/**
 * A frame in a TiledAnimation.
 */
public class TiledFrame {
    private int localId;
    private long duration;

    /**
     * @return The duration in milliseconds of the frame.
     */
    public long getDuration() {
        return duration;
    }

    /**
     * @param duration The duration in milliseconds of the frame.
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getLocalId() {
        return localId;
    }

    public void setLocalId(int localId) {
        this.localId = localId;
    }
}
