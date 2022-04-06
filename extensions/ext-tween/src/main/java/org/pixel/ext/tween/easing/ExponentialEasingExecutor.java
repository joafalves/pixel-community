package org.pixel.ext.tween.easing;

import org.pixel.math.MathHelper;

public class ExponentialEasingExecutor implements TweenEasingExecutor {
    @Override
    public float execute(float start, float end, float position) {
        position *= 2;
        if (position < 1) {
            return ((end - start) / 2) * MathHelper.pow(2, 10 * (position - 1)) + start;
        }

        --position;
        return ((end - start) / 2) * (-MathHelper.pow(2, -10 * position) + 2) + start;
    }
}
