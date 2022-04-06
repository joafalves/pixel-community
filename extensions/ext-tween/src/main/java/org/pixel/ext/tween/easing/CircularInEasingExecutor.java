package org.pixel.ext.tween.easing;

import org.pixel.math.MathHelper;

public class CircularInEasingExecutor implements TweenEasingExecutor {
    @Override
    public float execute(float start, float end, float position) {
        return -(end - start) * (MathHelper.sqrt(1 - position * position) - 1) + start;
    }
}
