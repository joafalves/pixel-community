/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.math;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BoundaryTest {

    @Test
    public void testCollision() {
        Boundary a, b, c;
        a = new Boundary(Vector2.zero(), new Vector2(10, 0), new Vector2(0, 10), new Vector2(10, 10));
        b = new Boundary(new Vector2(5, 5), new Vector2(20, 5), new Vector2(5, 20), new Vector2(20, 20));
        c = new Boundary(new Vector2(15, 15), new Vector2(20, 15), new Vector2(15, 20), new Vector2(20, 20));

        Assertions.assertTrue(a.overlaps(b));
        Assertions.assertFalse(a.overlaps(c));
        Assertions.assertTrue(b.overlaps(c));
    }

}
