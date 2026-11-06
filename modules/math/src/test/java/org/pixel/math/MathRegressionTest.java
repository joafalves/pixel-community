package org.pixel.math;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class MathRegressionTest {

    private static final float EPS = 1e-5f;

    @Test
    void rotateComposesWithPreviousTransform() {
        float angle = MathHelper.PI / 3f;

        // rotate(a) followed by rotate(b) must equal rotate(a + b) (the old implementation overwrote the 3x3)
        Matrix4 twoSteps = new Matrix4();
        twoSteps.rotate(angle, 0, 0, 1);
        twoSteps.rotate(angle, 0, 0, 1);

        Matrix4 oneStep = new Matrix4();
        oneStep.rotate(angle * 2, 0, 0, 1);

        assertMatrixEquals(oneStep, twoSteps);
    }

    @Test
    void rotateKeepsPreviousScale() {
        Matrix4 m = new Matrix4();
        m.scale(2f, 2f, 1f);
        m.rotate(MathHelper.PI / 2f, 0, 0, 1);

        // a rotation preserves the length of the basis vectors (scale must survive)
        float[][] a = m.toUnsafeArray();
        float len0 = (float) Math.sqrt(a[0][0] * a[0][0] + a[0][1] * a[0][1]);
        float len1 = (float) Math.sqrt(a[1][0] * a[1][0] + a[1][1] * a[1][1]);
        assertEquals(2f, len0, EPS);
        assertEquals(2f, len1, EPS);
    }

    @Test
    void rotateOnIdentityIsUnchanged() {
        // sprites rely on rotate() right after setIdentity()/translate(): result must equal the plain rotation matrix
        float angle = 0.7f;
        Matrix4 m = new Matrix4();
        m.translate(10, 20, 0);
        m.rotate(angle, 0, 0, 1);

        float c = MathHelper.cos(angle);
        float s = MathHelper.sin(-angle);
        float[][] a = m.toUnsafeArray();
        assertEquals(c, a[0][0], EPS);
        assertEquals(-s, a[0][1], EPS);
        assertEquals(s, a[1][0], EPS);
        assertEquals(c, a[1][1], EPS);
        assertEquals(10f, a[3][0], EPS);
        assertEquals(20f, a[3][1], EPS);
    }

    @Test
    void equalMatricesHaveEqualHashCodes() {
        Matrix4 a = new Matrix4();
        a.translate(1, 2, 3);
        Matrix4 b = new Matrix4(a);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());

        assertEquals(new Matrix3(), new Matrix3());
        assertEquals(new Matrix3().hashCode(), new Matrix3().hashCode());
        assertEquals(new Matrix2().hashCode(), new Matrix2().hashCode());
    }

    @Test
    void valueTypesHaveConsistentHashCodes() {
        assertEquals(new Vector3(1, 2, 3).hashCode(), new Vector3(1, 2, 3).hashCode());
        assertEquals(new Vector3(0f, 0f, 0f).hashCode(), new Vector3(-0f, 0f, 0f).hashCode());
        assertEquals(new Size(1, 2).hashCode(), new Size(1, 2).hashCode());
        assertEquals(new SizeInt(1, 2).hashCode(), new SizeInt(1, 2).hashCode());
        assertEquals(new Line(1, 2, 3, 4).hashCode(), new Line(1, 2, 3, 4).hashCode());
    }

    @Test
    void toArrayDoesNotExposeInternalRows() {
        Matrix4 m = new Matrix4();
        float[][] copy = m.toArray();
        copy[0][0] = 42f;
        assertEquals(1f, m.toUnsafeArray()[0][0]);
    }

    @Test
    void deserializedMatrixCanMultiply() throws Exception {
        Matrix4 m = roundTrip(new Matrix4());
        assertDoesNotThrow(() -> m.multiply(new Matrix4()));
        assertEquals(new Matrix4(), m);
    }

    @Test
    void vectorConstantsAreReadOnly() {
        assertThrows(UnsupportedOperationException.class, () -> Vector2.ZERO.set(1, 1));
        assertThrows(UnsupportedOperationException.class, () -> Vector2.ONE.add(1));
        assertThrows(UnsupportedOperationException.class, () -> Vector2.HALF.setX(3));
        assertThrows(UnsupportedOperationException.class, Vector2.UP::normalize);
        assertEquals(new Vector2(0, 0), Vector2.ZERO);
        assertEquals(new Vector2(0.5f, 0.5f), Vector2.HALF);
    }

    @Test
    void copiesOfVectorConstantsAreMutable() {
        Vector2 v = new Vector2(Vector2.ZERO);
        v.set(5, 6);
        assertEquals(new Vector2(5, 6), v);
        assertEquals(new Vector2(0, 0), Vector2.ZERO);

        Vector2 z = Vector2.zero();
        z.add(1);
        assertEquals(new Vector2(1, 1), z);
    }

    @Test
    void serializedVectorConstantBecomesMutableVector() throws Exception {
        Vector2 v = roundTrip(Vector2.ZERO);
        assertEquals(Vector2.class, v.getClass());
        v.set(1, 2);
        assertEquals(new Vector2(1, 2), v);
    }

    private static void assertMatrixEquals(Matrix4 expected, Matrix4 actual) {
        float[][] e = expected.toUnsafeArray();
        float[][] a = actual.toUnsafeArray();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                assertEquals(e[i][j], a[i][j], EPS, "m[" + i + "][" + j + "]");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T roundTrip(T value) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(value);
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            return (T) in.readObject();
        }
    }
}
