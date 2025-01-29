package org.pixel.commons;

public class Timer {
    private long intervalMs;
    private long elapsedMs;
    private long lastOverflowMs;

    /**
     * Constructor.
     *
     * @param intervalMs The time interval reference in milliseconds (must be positive)
     */
    public Timer(long intervalMs) {
        setInterval(intervalMs, true);
    }

    /**
     * Checks if the configured interval has passed. Use `getLastOverflow()` to get the overflow time.
     *
     * @param delta The DeltaTime reference containing elapsed milliseconds
     * @return True if the interval has been reached or exceeded
     */
    public boolean check(DeltaTime delta) {
        elapsedMs += delta.getElapsedMs();
        if (elapsedMs >= intervalMs) {
            lastOverflowMs = elapsedMs - intervalMs;
            elapsedMs = lastOverflowMs;  // Carry over excess
            return true;
        }
        return false;
    }

    /**
     * @return The overflow milliseconds from the last successful check
     */
    public long getLastOverflow() {
        return lastOverflowMs;
    }

    /**
     * Reset timer state (elapsed and overflow time)
     */
    public void reset() {
        this.elapsedMs = 0;
        this.lastOverflowMs = 0;
    }

    /**
     * Update interval with current progress preservation
     */
    public void setInterval(long intervalMs) {
        setInterval(intervalMs, false);
    }

    /**
     * Update interval with optional reset
     */
    public void setInterval(long intervalMs, boolean reset) {
        if (intervalMs <= 0) {
            throw new IllegalArgumentException("Interval must be positive!");
        }
        this.intervalMs = intervalMs;

        if (reset) {
            reset();
        } else if (this.elapsedMs > intervalMs) {
            // Force immediate trigger on next check while preserving overflow potential
            this.elapsedMs = intervalMs;
        }
    }
}