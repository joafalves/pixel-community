package org.pixel.ext.tween.easing;

public class BounceInEasingExecutor extends BounceOutEasingExecutor {
    @Override
    public float execute(float start, float end, float position) {
        return super.execute(end, start, 1f - position);
    }
}
