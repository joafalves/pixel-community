package org.pixel.ext.tween.easing;

public interface TweenEasingExecutor {
    /**
     * Tween easing function.
     *
     * @param start    The initial value.
     * @param end      The final value.
     * @param position The current position (normalized from 0 to 1).
     * @return Calculated value at the current position.
     */
    float execute(float start, float end, float position);
}
