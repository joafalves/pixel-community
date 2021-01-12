/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.commons;

public class Pair<A, B> {

    private A a;
    private B b;

    /**
     * Constructor
     *
     * @param a
     * @param b
     */
    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public A getA() {
        return a;
    }

    public B getB() {
        return b;
    }

    public void setA(A a) {
        this.a = a;
    }

    public void setB(B b) {
        this.b = b;
    }
}
