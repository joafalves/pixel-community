package org.pixel.ext.tween.easing;

import org.pixel.math.MathHelper;

public class LinearTweenEasingExecutor implements TweenEasingExecutor {
    @Override
    public float execute(float start, float end, float position) {
        return MathHelper.linearInterpolation(start, end, position);
    }
}
