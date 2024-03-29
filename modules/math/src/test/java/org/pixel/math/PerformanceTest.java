/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.math;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class PerformanceTest {

    private static final int ITERATIONS = 100000;

    @Test
    @Disabled
    public void matrix4MultiplyScalar() {
        Matrix4 mat = new Matrix4();

        long start = System.nanoTime();
        long elapsed;

        for (int i = 0; i < ITERATIONS; ++i) {
            mat.multiply(i);
        }

        elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        System.out.println("Elapsed: " + elapsed);
    }

    @Test
    @Disabled
    public void matrix4Multiply() {
        Matrix4 mat = new Matrix4();
        Matrix4 otherMat = new Matrix4();

        long start = System.nanoTime();
        long elapsed;

        for (int i = 0; i < ITERATIONS; ++i) {
            mat.multiply(otherMat);
        }

        elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        System.out.println("Elapsed: " + elapsed);
    }
}
