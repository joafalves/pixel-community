/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.math;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

public class PerformanceTest {

    private static final int ITERATIONS = 100000;

    @Test
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
    public void matrix4Multiply() {
        Matrix4 mat = new Matrix4();
        Matrix4 omat = new Matrix4();

        long start = System.nanoTime();
        long elapsed;

        for (int i = 0; i < ITERATIONS; ++i) {
            mat.multiply(omat);
        }

        elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        System.out.println("Elapsed: " + elapsed);
    }
}
