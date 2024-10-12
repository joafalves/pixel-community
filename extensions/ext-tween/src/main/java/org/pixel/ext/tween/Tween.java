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
    private String name;
    private TweenPhaseListener listener;
    private TweenEasingExecutor easingExecutors;
    private TweenLoopMode loopMode;
    private float[] start;
    private float[] end;
    private float[] instant;
    private float pauseDelay;
    private float elapsedSeconds;
    private float durationInSeconds;
    private Object target;

    /**
     * Constructor.
     *
     * @param start The initial values.
     */
    public Tween(float... start) {
        this.start = start;
        this.end = start;
        this.enabled = true;
        this.durationInSeconds = 1;
        this.easingExecutors = TweenEasingFactory.getExecutor(TweenEasingMode.LINEAR);
        this.loopMode = TweenLoopMode.NONE;
        this.pauseDelay = 0;
        this.reset();
    }

    /**
     * Constructor.
     *
     * @param start The instance to copy the initial values from (if possible).
     */
    public <T> Tween(T start) {
        this.from(start);
        this.to(start);
        if (this.start == null) {
            throw new IllegalArgumentException("The provided instance is not supported: " + start.getClass().getName());
        }
        this.enabled = true;
        this.durationInSeconds = 1;
        this.pauseDelay = 0;
        this.easingExecutors = TweenEasingFactory.getExecutor(TweenEasingMode.LINEAR);
        this.loopMode = TweenLoopMode.NONE;
        this.reset();
    }

    @Override
    public void update(DeltaTime delta) {
        if (!enabled) {
            return;
        }
        if (instant == null || instant.length != start.length) {
            instant = new float[start.length];
        }

        if (pauseDelay > 0) {
            pauseDelay -= delta.getElapsed();
            if (pauseDelay > 0) {
                return;
            }
        }

        float elapsedTmp = Math.min(elapsedSeconds + delta.getElapsed(), durationInSeconds);
        if (elapsedTmp > 0 && elapsedSeconds == 0 && listener != null) {
            listener.onTweenPhaseChange(this, TweenPhase.START);
        }
        elapsedSeconds = elapsedTmp;

        float timeNormalized = elapsedSeconds / durationInSeconds;
        for (int i = 0; i < instant.length; i++) {
            instant[i] = easingExecutors.execute(start[i], end[i], timeNormalized);
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

        if (target != null) {
            copyTo(target);
        }
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
        reset();
    }

    /**
     * Set the tween as disabled for the next update and reset the elapsed time.
     */
    public void stop() {
        this.enabled = false;
        reset();
    }

    /**
     * Pause the tween until the given seconds elapse.
     *
     * @param seconds The number of seconds that the tween shall pause.
     */
    public void pause(float seconds) {
        this.pauseDelay = seconds;
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
     * Set the name of this tween.
     *
     * @param name The name of this tween.
     * @return This instance.
     */
    public Tween name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Set the listener to be triggered when the tween state changes.
     *
     * @param listener The listener.
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
            this.start = new float[]{((Vector3) instance).getX(), ((Vector3) instance).getY(),
                    ((Vector3) instance).getZ()};
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
            this.end = new float[]{((Vector3) instance).getX(), ((Vector3) instance).getY(),
                    ((Vector3) instance).getZ()};
        } else {
            log.warn("Unsupported instance type '{}'.", instance.getClass().getName());
        }

        return this;
    }

    /**
     * Get the name of this tween.
     *
     * @return The name of this tween or null if not assigned.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the current value of this instance at the given index.
     *
     * @param index The index.
     * @return The current value.
     */
    public float getValue(int index) {
        if (index >= instant.length) {
            throw new IllegalArgumentException("Index out of bounds.");
        }

        return instant[index];
    }

    /**
     * Get the current value of this instance at the given index or return the given default value if index is out of
     * bounds.
     *
     * @param index        The index.
     * @param defaultValue The default value.
     * @return The current value or the default value if the index is out of bounds.
     */
    public float getValue(int index, float defaultValue) {
        if (index >= instant.length) {
            return defaultValue;
        }

        return instant[index];
    }

    /**
     * Copy the current values of this instance to the given instance (if possible).
     *
     * @param container The instance to copy the values to.
     */
    public void copyTo(Object container) {
        if (container == null) {
            throw new IllegalArgumentException("Container cannot be null.");
        }

        if (container instanceof Vector2 && instant.length >= 2) {
            ((Vector2) container).set(instant[0], instant[1]);

        } else if (container instanceof Vector3 && instant.length >= 3) {
            ((Vector3) container).set(instant[0], instant[1], instant[2]);

        } else if (container instanceof Rectangle && instant.length >= 4) {
            ((Rectangle) container).set(instant[0], instant[1], instant[2], instant[4]);

        } else if (container instanceof Size && instant.length >= 2) {
            ((Size) container).set(instant[0], instant[1]);

        } else if (container instanceof float[]) {
            System.arraycopy(instant, 0, ((float[]) container), 0, instant.length);

        } else {
            log.warn("Unsupported container type '{}'.", container.getClass().getName());
        }
    }

    /**
     * Reset the elapsed time of this instance.
     */
    private void reset() {
        elapsedSeconds = 0;
    }

    /**
     * Get the target instance of this tween.
     *
     * @return The target instance.
     */
    public Object  getTarget() {
        return target;
    }

    /**
     * Set the target instance of this tween.
     *
     * @param target The target instance.
     */
    public Tween target(Object target) {
        this.target = target;
        return this;
    }
}
