package org.pixel.core.dev;

import org.pixel.commons.logger.LogLevel;
import org.pixel.commons.logger.LogListener;

/**
 * The last messages logged (a ring buffer), for the dev tools' log panel. Thread-safe: messages can come from any
 * thread.
 */
final class DevLog implements LogListener {

    static final int CAPACITY = 200;

    private final LogLevel[] levels = new LogLevel[CAPACITY];
    private final String[] contexts = new String[CAPACITY];
    private final String[] messages = new String[CAPACITY];
    private int next;
    private int size;
    private int warnings;
    private int errors;

    @Override
    public synchronized void onLog(LogLevel level, String context, String message) {
        levels[next] = level;
        contexts[next] = context;
        messages[next] = message;
        next = (next + 1) % CAPACITY;
        size = Math.min(size + 1, CAPACITY);
        if (level == LogLevel.WARN) {
            warnings++;
        } else if (level == LogLevel.ERROR) {
            errors++;
        }
    }

    synchronized int size() {
        return size;
    }

    /** Warnings since the last {@link #clear()} (including those no longer kept). */
    synchronized int warnings() {
        return warnings;
    }

    synchronized int errors() {
        return errors;
    }

    /** Entry {@code age} (0 = newest). */
    synchronized LogLevel level(int age) {
        return levels[index(age)];
    }

    synchronized String context(int age) {
        return contexts[index(age)];
    }

    synchronized String message(int age) {
        return messages[index(age)];
    }

    /**
     * Copy the newest entries at once (consistent even while other threads log).
     *
     * @return How many were copied (newest first).
     */
    synchronized int snapshot(LogLevel[] levelsOut, String[] contextsOut, String[] messagesOut) {
        int n = Math.min(size, levelsOut.length);
        for (int age = 0; age < n; age++) {
            int i = index(age);
            levelsOut[age] = levels[i];
            contextsOut[age] = contexts[i];
            messagesOut[age] = messages[i];
        }
        return n;
    }

    synchronized void clear() {
        java.util.Arrays.fill(messages, null);
        java.util.Arrays.fill(contexts, null);
        size = 0;
        warnings = 0;
        errors = 0;
    }

    private int index(int age) {
        return ((next - 1 - age) % CAPACITY + CAPACITY) % CAPACITY;
    }
}
