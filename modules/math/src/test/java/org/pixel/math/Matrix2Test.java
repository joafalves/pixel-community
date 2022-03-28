package org.pixel.math;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Matrix2Test {

    @Test
    public void parityCheck() {
        var m1 = new Matrix2();
        var m2 = new Matrix2();
        var m3 = new Matrix2();
        m3.multiply(2);

        Assertions.assertEquals(m1, m2);
        Assertions.assertNotEquals(m1, m3);
    }

}
