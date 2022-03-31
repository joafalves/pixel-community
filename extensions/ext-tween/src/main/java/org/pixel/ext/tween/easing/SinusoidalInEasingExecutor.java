package org.pixel.ext.tween.easing;

import org.pixel.math.MathHelper;

public class SinusoidalInEasingExecutor implements TweenEasingExecutor {
    @Override
    public float execute(float start, float end, float position) {
        return -(end - start) * MathHelper.cos(position * MathHelper.PIo2)
                + (end - start) + start;
    }
}
