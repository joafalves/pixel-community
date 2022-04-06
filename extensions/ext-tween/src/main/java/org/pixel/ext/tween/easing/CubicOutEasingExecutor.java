package org.pixel.ext.tween.easing;

public class CubicOutEasingExecutor implements TweenEasingExecutor {
    @Override
    public float execute(float start, float end, float position) {
        position--;
        return (end - start) * (position * position * position + 1) + start;
    }
}
