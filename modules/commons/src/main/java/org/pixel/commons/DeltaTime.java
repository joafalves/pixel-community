/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.commons;

public class DeltaTime {

    private final long startTimestamp;
    private long lastTimestamp;
    private long elapsedMilliseconds;
    private float elapsedSeconds;

    /**
     * Constructor
     */
    public DeltaTime() {
        this.startTimestamp = System.currentTimeMillis();
        this.lastTimestamp = System.nanoTime() / 1000000;
    }

    /**
     * Get the elapsed time since the last tick (in seconds).
     *
     * @return Elapsed time in seconds.
     */
    public float getElapsed() {
        return elapsedSeconds;
    }

    /**
     * Get the elapsed time since the last tick (in milliseconds).
     *
     * @return Elapsed time in milliseconds.
     */
    public long getElapsedMs() {
        return elapsedMilliseconds;
    }

    /**
     * Get the total time elapsed since the initialization of this instance (in seconds).
     *
     * @return Total time elapsed in seconds.
     */
    public float getTotalElapsed() {
        return (System.currentTimeMillis() - startTimestamp) / 1000f;
    }

    /**
     * Get the total time elapsed since the initialization of this instance (in milliseconds).
     *
     * @return Total time elapsed in milliseconds.
     */
    public float getTotalElapsedMs() {
        return System.currentTimeMillis() - startTimestamp;
    }

    /**
     * Update elapsed time (calculates delta since last tick).
     */
    public void tick() {
        long now = System.nanoTime() / 1000000;
        elapsedMilliseconds = now - lastTimestamp;
        elapsedSeconds = elapsedMilliseconds / 1000f;
        lastTimestamp = now;
    }

}
