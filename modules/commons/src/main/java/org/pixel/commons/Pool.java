package org.pixel.commons;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Supplier;

public class Pool<T> {
    private final Queue<T> pool;
    private final Supplier<T> factory;

    public Pool(Supplier<T> factory, int initialSize) {
        this.factory = factory;
        this.pool = new ArrayDeque<>(initialSize);
        for (int i = 0; i < initialSize; i++) {
            pool.add(factory.get());
        }
    }

    public T obtain() {
        return pool.isEmpty() ? factory.get() : pool.poll();
    }

    public void free(T obj) {
        pool.offer(obj);
    }
}