package org.pixel.graphics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RenderStatsTest {

    @Test
    void recordedValuesOnlyBecomeVisibleWhenTheFrameEnds() {
        RenderStats stats = new RenderStats();
        stats.recordDrawCall(10, 60);
        stats.recordUpload(1024);
        stats.recordTextureBind();

        assertEquals(0, stats.lastFrame().getDrawCalls());
        assertEquals(0, stats.lastFrame().getFrameNumber());

        stats.endFrame();

        RenderStats.Frame frame = stats.lastFrame();
        assertEquals(1, frame.getFrameNumber());
        assertEquals(1, frame.getDrawCalls());
        assertEquals(10, frame.getPrimitives());
        assertEquals(60, frame.getVertices());
        assertEquals(1024, frame.getUploadedBytes());
        assertEquals(1, frame.getTextureBinds());
    }

    @Test
    void eachFrameStartsFromZero() {
        RenderStats stats = new RenderStats();
        stats.recordDrawCall(5, 30);
        stats.recordDrawCall(5, 30);
        stats.endFrame();
        stats.recordDrawCall(1, 6);
        stats.endFrame();

        assertEquals(2, stats.lastFrame().getFrameNumber());
        assertEquals(1, stats.lastFrame().getDrawCalls());
        assertEquals(1, stats.lastFrame().getPrimitives());
    }

    @Test
    void emptyFramesArePublishedToo() {
        RenderStats stats = new RenderStats();
        stats.recordDrawCall(5, 30);
        stats.endFrame();
        stats.endFrame();

        assertEquals(2, stats.lastFrame().getFrameNumber());
        assertEquals(0, stats.lastFrame().getDrawCalls());
    }
}
