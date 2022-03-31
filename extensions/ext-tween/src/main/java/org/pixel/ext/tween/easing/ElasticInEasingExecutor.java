package org.pixel.ext.tween.easing;

import org.pixel.math.MathHelper;

public class ElasticInEasingExecutor implements TweenEasingExecutor {

    private static final float FACTOR = (2 * MathHelper.PI) / 3;

    @Override
    public float execute(float start, float end, float position) {
        if (position <= 0.00001f) return start;
        if (position >= 0.999f) return end;

        float p = .3f;
        float diff = end - start;
        float s = p / 4f;

        float postFix = diff * MathHelper.pow(2, 10 * (position -= 1));
        return -(postFix * MathHelper.sin((position - s) * (2 * MathHelper.PI) / p)) + start;
    }
}
