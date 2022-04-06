package org.pixel.ext.tween.easing;

public class BounceOutEasingExecutor implements TweenEasingExecutor {

    private static final float FACTOR = 7.5625f;
    private static final float DISTANCE = 2.75f;

    @Override
    public float execute(float start, float end, float position) {
        float diff = end - start;
        if (position < (1 / DISTANCE)) {
            return diff * (FACTOR * position * position) + start;

        } else if (position < (2.0f / DISTANCE)) {
            float postFix = position -= (1.5f / DISTANCE);
            return diff * (FACTOR * (postFix) * position + .75f) + start;

        } else if (position < (2.5f / DISTANCE)) {
            float postFix = position -= (2.25f / DISTANCE);
            return diff * (FACTOR * (postFix) * position + .9375f) + start;
        }

        float postFix = position -= (2.625f / DISTANCE);
        return diff * (FACTOR * (postFix) * position + .984375f) + start;
    }
}
