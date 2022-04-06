package org.pixel.ext.tween.easing;

import org.pixel.math.MathHelper;

public class ElasticOutEasingExecutor implements TweenEasingExecutor {
    @Override
    public float execute(float start, float end, float position) {
        if (position <= 0.00001f) return start;
        if (position >= 0.999f) return end;

        float p = .3f;
        float diff = end - start;
        float s = p / 4f;

        return diff * MathHelper.pow(2, -10 * position) * MathHelper.sin((position - s) * (2 * MathHelper.PI) / p) + end;
    }
}
