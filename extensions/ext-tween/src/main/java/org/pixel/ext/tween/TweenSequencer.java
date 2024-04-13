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
            enabled = false;
            return;
        }

        tweenSequence[currentTween].update(delta);
    }

    /**
     * Set the tween as active for the next update and resets the sequence progression.
     */
    public void restart() {
        this.changeCurrentTween(0);
        this.enabled = true;
    }

    /**
     * Set the tween as disabled for the next update and resets the sequence progression.
     */
    public void stop() {
        this.changeCurrentTween(0);
        this.enabled = false;
    }

    /**
     * Set the tween sequencer as disabled for the next update.
     */
    public void pause() {
        this.enabled = false;
    }

    /**
     * Sets the tween sequencer as active for the next update.
     */
    public void play() {
        this.enabled = true;
    }

    /**
     * Set the sequence of tween instances to manage.
     *
     * @param tweenSequence The sequence of tween instances to manage.
     * @return This instance.
     */
    public TweenSequencer set(Tween... tweenSequence) {
        this.tweenSequence = tweenSequence;
        this.changeCurrentTween(0);
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
     * Get the currently assigned tween sequence.
     *
     * @return The currently assigned tween sequence.
     */
    public Tween[] getTweenSequence() {
        return tweenSequence;
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

    /**
     * Get the active tween index.
     *
     * @return The active tween index or '-1' if none is active.
     */
    public int getActiveTweenIndex() {
        if (!enabled || currentTween >= tweenSequence.length) {
            return -1;
        }

        return currentTween;
    }

    private void changeCurrentTween(int index) {
        this.currentTween = index;
        if (currentTween < tweenSequence.length) {
            // Set the event handler, so we can change the current tween when the current one finishes
            tweenSequence[currentTween].on((tween, phase) -> {
                if (phase == TweenPhase.END) {
                    currentTween += 1;
                    if (currentTween >= tweenSequence.length) {
                        // no more tweens to play
                        enabled = false;
                        if (listener != null) {
                            listener.onSequencerCompleted();
                        }
                    } else {
                        changeCurrentTween(currentTween);
                    }
                }
            });
        }
    }
}
