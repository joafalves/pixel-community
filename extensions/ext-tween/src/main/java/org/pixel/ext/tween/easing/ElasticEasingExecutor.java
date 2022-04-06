package org.pixel.ext.tween.easing;

import org.pixel.math.MathHelper;

public class ElasticEasingExecutor implements TweenEasingExecutor {
    @Override
    public float execute(float start, float end, float position) {
        if (position <= 0.00001f) return start;
        if (position >= 0.999f) return end;

        position *= 2;
        float p = (.3f * 1.5f);
        float diff = end - start;
        float s = p / 4;
        float postFix;

        if (position < 1) {
            postFix = diff * MathHelper.pow(2, 10 * (position -= 1));
            return -0.5f * (postFix * MathHelper.sin((position - s) * (2 * MathHelper.PI) / p)) + start;
        }

        postFix = diff * MathHelper.pow(2, -10 * (position -= 1));
        return postFix * MathHelper.sin((position - s) * (2 * MathHelper.PI) / p) * .5f + end;
    }
}
