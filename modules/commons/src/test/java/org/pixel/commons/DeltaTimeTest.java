package org.pixel.commons;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeltaTimeTest {

    @Test
    void elapsedTimeHasSubMillisecondPrecision() throws InterruptedException {
        DeltaTime delta = new DeltaTime();
        boolean fractional = false;
        for (int i = 0; i < 50 && !fractional; i++) {
            long until = System.nanoTime() + 300_000; // about 0.3 ms
            while (System.nanoTime() < until) {
                Thread.onSpinWait();
            }
            delta.tick();
            float ms = delta.getElapsed() * 1000f;
            assertTrue(ms > 0f, "never zero for a real interval");
            fractional = Math.abs(ms - Math.round(ms)) > 1e-3f;
        }
        assertTrue(fractional, "intervals are not whole milliseconds");
    }
}
