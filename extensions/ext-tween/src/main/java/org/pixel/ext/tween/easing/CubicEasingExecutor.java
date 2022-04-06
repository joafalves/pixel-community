package org.pixel.ext.tween.easing;

public class CubicEasingExecutor implements TweenEasingExecutor {
    @Override
    public float execute(float start, float end, float position) {
        position *= 2;
        if (position < 1) {
            return((end - start) / 2) * position * position * position + start;
        }

        position -= 2;
        return ((end - start) / 2) * (position * position * position + 2) + start;
    }
}
