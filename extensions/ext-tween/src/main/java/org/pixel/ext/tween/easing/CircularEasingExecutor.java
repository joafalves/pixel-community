package org.pixel.ext.tween.easing;

import org.pixel.math.MathHelper;

public class CircularEasingExecutor implements TweenEasingExecutor {
    @Override
    public float execute(float start, float end, float position) {
        position *= 2;
        if (position < 1) {
            return (-(end - start) / 2) * (MathHelper.sqrt(1 - position * position) - 1) + start;
        }

        position -= 2;
        return ((end - start) / 2) * (MathHelper.sqrt(1 - position * position) + 1) + start;
    }
}
