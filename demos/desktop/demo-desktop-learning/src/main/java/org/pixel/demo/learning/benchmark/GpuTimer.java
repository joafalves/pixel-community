package org.pixel.demo.learning.benchmark;

import static org.lwjgl.opengl.GL15C.glBeginQuery;
import static org.lwjgl.opengl.GL15C.glDeleteQueries;
import static org.lwjgl.opengl.GL15C.glEndQuery;
import static org.lwjgl.opengl.GL15C.glGenQueries;
import static org.lwjgl.opengl.GL15C.glGetQueryObjecti;
import static org.lwjgl.opengl.GL15C.GL_QUERY_RESULT_AVAILABLE;
import static org.lwjgl.opengl.GL33C.GL_TIME_ELAPSED;
import static org.lwjgl.opengl.GL33C.glGetQueryObjecti64;
import static org.lwjgl.opengl.GL15C.GL_QUERY_RESULT;

/**
 * Measures GPU time of a section of the frame with GL_TIME_ELAPSED queries (core since GL 3.3).
 * <p>
 * Uses a small ring of queries and reads results a few frames late, so measuring never stalls the pipeline.
 */
final class GpuTimer {

    private static final int RING = 4;

    private final int[] queries = new int[RING];
    private final boolean[] pending = new boolean[RING];
    private int frame;
    private int skip;
    private double lastResultMs = -1;

    GpuTimer() {
        glGenQueries(queries);
    }

    void begin() {
        int slot = frame % RING;
        // collect the oldest result before reusing its query object
        if (pending[slot]) {
            if (skip > 0) {
                skip--;
                lastResultMs = -1; // result belongs to frames before the last discard()
            } else if (glGetQueryObjecti(queries[slot], GL_QUERY_RESULT_AVAILABLE) != 0) {
                lastResultMs = glGetQueryObjecti64(queries[slot], GL_QUERY_RESULT) / 1e6;
            } else {
                lastResultMs = -1; // not ready yet (GPU far behind): skip this sample rather than block
            }
            pending[slot] = false;
        }
        glBeginQuery(GL_TIME_ELAPSED, queries[slot]);
    }

    void end() {
        glEndQuery(GL_TIME_ELAPSED);
        pending[frame % RING] = true;
        frame++;
    }

    /**
     * @return The GPU time (ms) of the section measured {@code RING} frames ago, or -1 if unavailable.
     */
    double lastResultMs() {
        return lastResultMs;
    }

    /**
     * Ignore the results of queries already in flight (e.g. frames from a previous scenario), so the next samples only
     * cover frames issued after this call.
     */
    void discardPending() {
        skip = 0;
        for (boolean p : pending) {
            if (p) skip++;
        }
        lastResultMs = -1;
    }

    void dispose() {
        glDeleteQueries(queries);
    }
}
