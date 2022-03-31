package org.pixel.ext.tween.easing;

import org.pixel.math.MathHelper;

public class SinusoidalEasingExecutor implements TweenEasingExecutor {
    @Override
    public float execute(float start, float end, float position) {
        return (-(end - start) / 2) * (MathHelper.cos(position * MathHelper.PI) - 1) + start;
    }
}
