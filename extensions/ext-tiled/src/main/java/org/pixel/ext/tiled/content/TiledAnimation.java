package org.pixel.ext.tiled.content;

import java.util.List;

public class TiledAnimation {
    List<TiledFrame> frameList;
    private int totalFrameCount;

    public TiledAnimation() {
        this.totalFrameCount = 0;
    }

    public List<TiledFrame> getFrameList() {
        return frameList;
    }

    public void setFrameList(List<TiledFrame> frameList) {
        this.frameList = frameList;
        this.totalFrameCount = 0;

        for (TiledFrame frame : frameList) {
            totalFrameCount += frame.getDuration();
        }
    }

    public int getTotalFrameCount() {
        return totalFrameCount;
    }
}
