package org.pixel.graphics.backend;

import org.pixel.content.Texture;

import java.util.Arrays;

/**
 * Inputs of one full-screen effect draw: up to {@link #MAX_INPUTS} textures (sampled as {@code uSource},
 * {@code uInput1}, {@code uInput2}, {@code uInput3}) and named float uniforms of 1 to 4 components. Reusable and
 * allocation-free once its arrays have grown; uniform names should be constants (they are compared by value).
 */
public final class PostParams {

    public static final int MAX_INPUTS = 4;

    private final Texture[] inputs = new Texture[MAX_INPUTS];
    private String[] names = new String[8];
    private float[] values = new float[8 * 4];
    private int[] sizes = new int[8];
    private int count;

    /**
     * Forget all inputs and uniforms.
     */
    public PostParams clear() {
        Arrays.fill(inputs, null);
        count = 0;
        return this;
    }

    /**
     * Set an input texture: 0 is {@code uSource} (whose size gives {@code uTexel}), 1-3 are {@code uInputN}.
     */
    public PostParams input(int index, Texture texture) {
        inputs[index] = texture;
        return this;
    }

    public PostParams set(String name, float x) {
        return put(name, 1, x, 0f, 0f, 0f);
    }

    public PostParams set(String name, float x, float y) {
        return put(name, 2, x, y, 0f, 0f);
    }

    public PostParams set(String name, float x, float y, float z, float w) {
        return put(name, 4, x, y, z, w);
    }

    private PostParams put(String name, int size, float x, float y, float z, float w) {
        int i = indexOf(name);
        if (i < 0) {
            if (count == names.length) {
                names = Arrays.copyOf(names, count * 2);
                values = Arrays.copyOf(values, count * 2 * 4);
                sizes = Arrays.copyOf(sizes, count * 2);
            }
            i = count++;
            names[i] = name;
        }
        sizes[i] = size;
        values[i * 4] = x;
        values[i * 4 + 1] = y;
        values[i * 4 + 2] = z;
        values[i * 4 + 3] = w;
        return this;
    }

    private int indexOf(String name) {
        for (int i = 0; i < count; i++) {
            if (names[i].equals(name)) {
                return i;
            }
        }
        return -1;
    }

    public Texture input(int index) {
        return inputs[index];
    }

    public int uniformCount() {
        return count;
    }

    public String uniformName(int index) {
        return names[index];
    }

    /** Components of a uniform (1, 2 or 4). */
    public int uniformSize(int index) {
        return sizes[index];
    }

    public float uniformValue(int index, int component) {
        return values[index * 4 + component];
    }
}
