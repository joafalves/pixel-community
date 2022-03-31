package org.pixel.ext.tween.easing;

import org.pixel.math.MathHelper;

public class ExponentialOutEasingExecutor implements TweenEasingExecutor {
    @Override
    public float execute(float start, float end, float position) {
        return (end - start) * (-MathHelper.pow(2, -10 * position) + 1) + start;
    }
}
