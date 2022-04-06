package org.pixel.ext.tween.easing;

public class BounceEasingExecutor extends BounceOutEasingExecutor {
    @Override
    public float execute(float start, float end, float position) {
        position *= 2f;
        if (position < 1f) {
            return super.execute(end, start, 1f - position);
        }

        return super.execute(start, end, 2f - position);
    }
}
