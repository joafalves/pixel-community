/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.commons.data;

public class Pair<A, B> {

    private A a;
    private B b;

    /**
     * Constructor.
     *
     * @param a A value.
     * @param b B value.
     */
    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    /**
     * Get A value.
     *
     * @return A value.
     */
    public A getA() {
        return a;
    }

    /**
     * Get B value.
     *
     * @return B value.
     */
    public B getB() {
        return b;
    }

    /**
     * Set A value.
     *
     * @param a A value.
     */
    public void setA(A a) {
        this.a = a;
    }

    /**
     * Set B value.
     *
     * @param b B value.
     */
    public void setB(B b) {
        this.b = b;
    }
}
