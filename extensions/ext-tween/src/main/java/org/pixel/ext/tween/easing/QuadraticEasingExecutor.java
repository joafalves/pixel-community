package org.pixel.ext.tween.easing;

public class QuadraticEasingExecutor implements TweenEasingExecutor {
    @Override
    public float execute(float start, float end, float position) {
        position *= 2;
        if (position < 1) {
            return ((end - start) / 2) * position * position + start;
        }

        position--;
        return (-(end - start) / 2) * (position * (position - 2) - 1) + start;
    }
}
