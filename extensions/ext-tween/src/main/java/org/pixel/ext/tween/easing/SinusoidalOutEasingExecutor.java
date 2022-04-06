package org.pixel.ext.tween.easing;

import org.pixel.math.MathHelper;

public class SinusoidalOutEasingExecutor implements TweenEasingExecutor {
    @Override
    public float execute(float start, float end, float position) {
        return (end - start) * MathHelper.sin(position * MathHelper.PIo2) + start;
    }
}
