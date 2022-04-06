package org.pixel.ext.tween;

import org.pixel.commons.DeltaTime;
import org.pixel.commons.lifecycle.Updatable;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.ext.tween.easing.TweenEasingExecutor;
import org.pixel.math.Rectangle;
import org.pixel.math.Size;
import org.pixel.math.Vector2;
import org.pixel.math.Vector3;

public class Tween implements Updatable {

    private static final Logger log = LoggerFactory.getLogger(Tween.class);

    private boolean enabled;
    private TweenPhaseListener listener;
    private TweenEasingExecutor easingExecutors;
    private TweenLoopMode loopMode;
    private float[] start;
    private float[] end;
    private float[] current;
    private float elapsedSeconds;
    private float durationInSeconds;

    /**
     * Constructor.
     *
     * @param start The initial values.
     */
    public Tween(float... start) {
        this.start = start;
        this.enabled = true;
        this.durationInSeconds = 1;
        this.easingExecutors = TweenEasingFactory.getExecutor(TweenEasingMode.LINEAR);
        this.loopMode = TweenLoopMode.NONE;
        this.elapsedSeconds = 0;
    }

    /**
     * Constructor.
     *
     * @param start The instance to copy the initial values from (if possible).
     */
    public <T> Tween(T start) {
        this.from(start);
        if (this.start == null) {
            throw new IllegalArgumentException("The provided instance is not supported: " + start.getClass().getName());
        }
        this.enabled = true;
        this.durationInSeconds = 1;
        this.easingExecutors = TweenEasingFactory.getExecutor(TweenEasingMode.LINEAR);
        this.loopMode = TweenLoopMode.NONE;
        this.elapsedSeconds = 0;
    }

    @Override
    public void update(DeltaTime delta) {
        if (!enabled) return;
        if (current == null || current.length != start.length) {
            current = new float[start.length];
        }

        float elapsedTmp = Math.min(elapsedSeconds + delta.getElapsed(), durationInSeconds);
        if (elapsedTmp > 0 && elapsedSeconds == 0 && listener != null) {
            listener.onTweenPhaseChange(this, TweenPhase.START);
        }
        elapsedSeconds = elapsedTmp;

        float timeNormalized = elapsedSeconds / durationInSeconds;
        for (int i = 0; i < current.length; i++) {
            current[i] = easingExecutors.execute(start[i], end[i], timeNormalized);
        }

        if (elapsedSeconds >= durationInSeconds) { // finished?
            if (listener != null) {
                listener.onTweenPhaseChange(this, TweenPhase.END);
            }

            elapsedSeconds = 0;
            switch (loopMode) {
                case NONE:
                    stop();
                    break;
                case LOOP_REVERSE:
                    float[] tmp = start;
                    start = end;
                    end = tmp;
                    break;
            }
        }
    }

    /**
     * Update function. Copy the current values to the given target instance (if possible).
     *
     * @param delta  The delta time.
     * @param target The target instance to receive the current values.
     * @param <T>    The type of the target instance.
     */
    public <T> void update(DeltaTime delta, T target) {
        this.update(delta);
        this.copyTo(target);
    }

    /**
     * Get the current playing state.
     *
     * @return True if the tween is playing, false otherwise.
     */
    public boolean isPlaying() {
        return enabled;
    }

    /**
     * Set the tween as active for the next update and reset the elapsed time.
     */
    public void restart() {
        this.enabled = true;
        this.elapsedSeconds = 0;
    }

    /**
     * Set the tween as disabled for the next update and reset the elapsed time.
     */
    public void stop() {
        this.enabled = false;
        this.elapsedSeconds = 0;
    }

    /**
     * Set the tween as disabled for the next update.
     */
    public void pause() {
        this.enabled = false;
    }

    /**
     * Sets the tween as active for the next update.
     */
    public void play() {
        this.enabled = true;
    }

    /**
     * Set the listener to be notified when the tween state changes.
     *
     * @param listener The listener to be notified.
     * @return This instance.
     */
    public Tween on(TweenPhaseListener listener) {
        this.listener = listener;
        return this;
    }

    /**
     * Set the easing executor of this instance.
     *
     * @param easingExecutor The easing executor of this instance.
     * @return This instance.
     */
    public Tween easing(TweenEasingExecutor easingExecutor) {
        this.easingExecutors = easingExecutor;
        return this;
    }

    /**
     * Set the easing executor of this instance.
     *
     * @param easing The easing mode.
     * @return This instance.
     * @throws RuntimeException If the easing executor is not found.
     */
    public Tween easing(TweenEasingMode easing) throws RuntimeException {
        return easing(easing.name());
    }

    /**
     * Set the easing executor of this instance.
     *
     * @param easingExecutorIdentity The identity of the easing executor.
     * @return This instance.
     * @throws RuntimeException If the easing executor is not found.
     */
    public Tween easing(String easingExecutorIdentity) throws RuntimeException {
        var executor = TweenEasingFactory.getExecutor(easingExecutorIdentity);
        if (executor == null) {
            throw new RuntimeException("Easing executor not found for easing: " + easingExecutorIdentity);
        }

        this.easingExecutors = executor;
        return this;
    }

    /**
     * Set the loop mode of this instance.
     *
     * @param loopMode The loop mode.
     * @return This instance.
     */
    public Tween loopMode(TweenLoopMode loopMode) {
        this.loopMode = loopMode;
        return this;
    }

    /**
     * Set the duration of this instance (in seconds).
     *
     * @param seconds The duration in seconds.
     * @return This instance.
     */
    public Tween duration(float seconds) {
        this.durationInSeconds = seconds;
        return this;
    }

    /**
     * Set the initial values of this instance.
     *
     * @param start The initial values.
     * @return This instance.
     */
    public Tween from(float... start) {
        this.start = start;
        return this;
    }

    /**
     * Set the target values of this instance based on the given instance (if possible).
     *
     * @param instance The instance to take the values from.
     * @return This instance.
     */
    public <T> Tween from(T instance) {
        if (instance instanceof Vector2) {
            this.start = new float[]{((Vector2) instance).getX(), ((Vector2) instance).getY()};
        } else if (instance instanceof Vector3) {
            this.start = new float[]{((Vector3) instance).getX(), ((Vector3) instance).getY(), ((Vector3) instance).getZ()};
        } else {
            log.warn("Unsupported instance type '{}'.", instance.getClass().getName());
        }

        return this;
    }

    /**
     * Set the target values of this instance.
     *
     * @param end The target values.
     * @return This instance.
     */
    public Tween to(float... end) {
        if (end.length != this.start.length) {
            throw new IllegalArgumentException("Target values must be of same length as initially assigned.");
        }

        this.end = end;
        return this;
    }

    /**
     * Set the target values of this instance based on the given instance (if possible).
     *
     * @param instance The instance to take the values from.
     * @return This instance.
     */
    public <T> Tween to(T instance) {
        if (instance instanceof Vector2) {
            this.end = new float[]{((Vector2) instance).getX(), ((Vector2) instance).getY()};
        } else if (instance instanceof Vector3) {
            this.end = new float[]{((Vector3) instance).getX(), ((Vector3) instance).getY(), ((Vector3) instance).getZ()};
        } else {
            log.warn("Unsupported instance type '{}'.", instance.getClass().getName());
        }

        return this;
    }

    /**
     * Get the current value of this instance at the given index.
     *
     * @param index The index.
     * @return The current value.
     */
    public float getValue(int index) {
        if (index > current.length) {
            throw new IllegalArgumentException("Index out of bounds.");
        }

        return current[index];
    }

    /**
     * Copy the current values of this instance to the given instance (if possible).
     *
     * @param container The instance to copy the values to.
     * @param <T>       The type of the instance.
     */
    public <T> void copyTo(T container) {
        if (container == null) {
            throw new IllegalArgumentException("Container cannot be null.");
        }

        if (container instanceof Vector2 && current.length >= 2) {
            ((Vector2) container).set(current[0], current[1]);

        } else if (container instanceof Vector3 && current.length >= 3) {
            ((Vector3) container).set(current[0], current[1], current[2]);

        } else if (container instanceof Rectangle && current.length >= 4) {
            ((Rectangle) container).set(current[0], current[1], current[2], current[4]);

        } else if (container instanceof Size && current.length >= 2) {
            ((Size) container).set(current[0], current[1]);

        } else {
            log.warn("Unsupported container type '{}'.", container.getClass().getName());
        }
    }
}
