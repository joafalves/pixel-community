package org.pixel.ext.tween;

import org.pixel.commons.DeltaTime;
import org.pixel.commons.lifecycle.Updatable;

public class TweenSequencer implements Updatable {

    private boolean enabled;
    private int currentTween;
    private TweenSequencerListener listener;
    private Tween[] tweenSequence;

    /**
     * Constructor.
     *
     * @param tweenSequence The sequence of tween instances to manage.
     */
    public TweenSequencer(Tween... tweenSequence) {
        this.enabled = true;
        this.set(tweenSequence);
    }

    @Override
    public void update(DeltaTime delta) {
        if (!enabled || currentTween >= tweenSequence.length) {
            return;
        }

        if (!tweenSequence[currentTween].isPlaying()) {
            currentTween++;
            if (currentTween >= tweenSequence.length) {
                enabled = false;
                if (listener != null) {
                    listener.onSequencerCompleted();
                }
                return;
            }
        }

        tweenSequence[currentTween].update(delta);
    }

    /**
     * Set the sequence of tween instances to manage.
     *
     * @param tweenSequence The sequence of tween instances to manage.
     * @return This instance.
     */
    public TweenSequencer set(Tween... tweenSequence) {
        this.currentTween = 0;
        this.tweenSequence = tweenSequence;
        return this;
    }

    /**
     * Set the listener to be triggered when the sequencer completes all tween instances.
     *
     * @param listener The listener.
     * @return This instance.
     */
    public TweenSequencer onComplete(TweenSequencerListener listener) {
        this.listener = listener;
        return this;
    }

    /**
     * Get the active tween at the moment of calling the function.
     *
     * @return The active tween or null if none is active.
     */
    public Tween getActiveTween() {
        if (!enabled || currentTween >= tweenSequence.length) {
            return null;
        }

        return tweenSequence[currentTween];
    }
}
