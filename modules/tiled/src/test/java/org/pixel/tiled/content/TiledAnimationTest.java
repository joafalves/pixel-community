package org.pixel.tiled.content;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

class TiledAnimationTest {
    @Test
    void setFrames() {
        TiledAnimation tiledAnimation = new TiledAnimation();
        List<TiledFrame> frames = new ArrayList<>();

        TiledFrame frame1 = Mockito.mock(TiledFrame.class);
        TiledFrame frame2 = Mockito.mock(TiledFrame.class);
        TiledFrame frame3 = Mockito.mock(TiledFrame.class);

        Mockito.when(frame1.getDuration()).thenReturn(10L);
        Mockito.when(frame2.getDuration()).thenReturn(25L);
        Mockito.when(frame3.getDuration()).thenReturn(7L);

        frames.add(frame1);
        frames.add(frame2);
        frames.add(frame3);

        tiledAnimation.setFrameList(frames);

        Assertions.assertEquals(10L + 25L + 7L, tiledAnimation.getTotalFrameCount());
    }
}