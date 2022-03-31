package org.pixel.ext.tween.easing;

public class QuadraticOutEasingExecutor implements TweenEasingExecutor {
    @Override
    public float execute(float start, float end, float position) {
        return (-(end - start) * position * (position - 2) + start);
    }
}
