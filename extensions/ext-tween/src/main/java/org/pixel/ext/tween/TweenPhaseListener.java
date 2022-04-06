package org.pixel.ext.tween;

public interface TweenPhaseListener {
    /**
     * Event when the tween phase is changed.
     *
     * @param tween The Tween instance.
     * @param phase The Tween phase state.
     */
    void onTweenPhaseChange(Tween tween, TweenPhase phase);
}
