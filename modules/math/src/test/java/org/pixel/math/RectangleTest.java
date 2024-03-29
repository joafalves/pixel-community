/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.math;

import org.junit.jupiter.api.Test;

public class RectangleTest {

    @Test
    public void unionTest() {
        Rectangle a = new Rectangle(10, 10, 10, 10);
        Rectangle b = new Rectangle(15, 15, 10, 10);
        a.union(b);
        System.out.println(a);
    }

    @Test
    public void intersectionTest() {
        Rectangle a = new Rectangle(10, 10, 10, 40);
        Rectangle b = new Rectangle(15, 15, 10, 10);

        if (Rectangle.overlaps(a, b)) {
            a.intersection(b);
        }
        System.out.println(a);
    }
}
