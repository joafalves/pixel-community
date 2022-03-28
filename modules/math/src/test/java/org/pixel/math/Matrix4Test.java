package org.pixel.math;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Matrix4Test {

    @Test
    public void parityCheck() {
        var m1 = new Matrix4();
        var m2 = new Matrix4();
        var m3 = new Matrix4();
        m3.translate(1, 1, 1);

        Assertions.assertEquals(m1, m2);
        Assertions.assertNotEquals(m1, m3);
    }

}
