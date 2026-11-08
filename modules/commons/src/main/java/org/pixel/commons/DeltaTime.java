/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.commons;

public class DeltaTime {

    private final long startTimestamp; // System.nanoTime(): monotonic, unaffected by clock changes
    private long lastTimestamp;        // nanoseconds
    private long elapsedMilliseconds;
    private float elapsedSeconds;

    /**
     * Constructor.
     */
    public DeltaTime() {
        this.startTimestamp = System.nanoTime();
        this.lastTimestamp = startTimestamp;
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
        return (System.nanoTime() - startTimestamp) / 1e9f;
    }

    /**
     * Get the total time elapsed since the initialization of this instance (in milliseconds).
     *
     * @return Total time elapsed in milliseconds.
     */
    public float getTotalElapsedMs() {
        return (System.nanoTime() - startTimestamp) / 1e6f;
    }

    /**
     * Update elapsed time (calculates delta since last tick).
     */
    public void tick() {
        // nanosecond precision: in whole milliseconds, a 1.4 ms frame would read as 1 or 2 ms (games jitter at high
        // frame rates, and frame statistics swing by a factor of two)
        long now = System.nanoTime();
        long nanos = now - lastTimestamp;
        elapsedSeconds = nanos / 1e9f;
        elapsedMilliseconds = Math.round(nanos / 1e6);
        lastTimestamp = now;
    }

    /**
     * Start measuring from now: the next {@link #tick()} measures only the time since this call. Hosts call it when
     * the loop resumes after a pause (e.g. an Android app back from the background), so the first frame's elapsed
     * time is not the whole pause (which would move everything far ahead at once).
     */
    public void restart() {
        lastTimestamp = System.nanoTime();
    }

}
