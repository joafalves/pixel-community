package org.pixel.commons;

public class Timer {
    private long intervalNs;
    private long lastTime;

    /**
     * Constructor.
     *
     * @param intervalMs The time interval reference in milliseconds (must be positive)
     */
    public Timer(long intervalMs) {
        this.intervalNs = intervalMs * 1_000_000L;
        this.lastTime = System.nanoTime();
    }

    /**
     * Checks if the configured interval has passed. Use `getLastOverflow()` to get the overflow time.
     *
     * @return True if the interval has been reached or exceeded
     */
    public boolean elapsed() {
        long now = System.nanoTime();
        long diff = now - lastTime;

        if (diff >= intervalNs) {
            lastTime += intervalNs;
            return true;
        }
        return false;
    }

    /**
     * Reset timer state (elapsed and overflow time)
     */
    public void reset() {
        this.lastTime = System.nanoTime();
    }

    /**
     * Update interval with current progress preservation
     *
     * @param intervalMs The time interval reference in milliseconds (must be positive)
     */
    public void setInterval(long intervalMs) {
        setInterval(intervalMs, false);
    }

    /**
     * Update interval with optional reset
     *
     * @param intervalMs The time interval reference in milliseconds (must be positive)
     * @param reset      If true, reset the timer to the current time
     */
    public void setInterval(long intervalMs, boolean reset) {
        this.intervalNs = intervalMs * 1_000_000L;
        if (reset) {
            this.lastTime = System.nanoTime();
        }
    }
}