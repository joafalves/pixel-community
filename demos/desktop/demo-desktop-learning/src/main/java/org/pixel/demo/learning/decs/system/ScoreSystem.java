package org.pixel.demo.learning.decs.system;

import org.pixel.commons.event.EventBus;
import org.pixel.demo.learning.decs.event.ScoreEvent;
import org.pixel.ext.decs.GameSystem;

import java.util.function.Consumer;

public class ScoreSystem extends GameSystem {

    private final EventBus eventBus;
    private final Consumer<ScoreEvent> scoreEventHandler;
    private int score;
    private int multiplier;

    public ScoreSystem(EventBus eventBus) {
        this.eventBus = eventBus;
        this.scoreEventHandler = this::onScoreEvent;
        this.score = 0;
        this.multiplier = 1;
    }

    @Override
    public void load() {
        eventBus.subscribe(ScoreEvent.class, scoreEventHandler);
    }

    private void onScoreEvent(ScoreEvent event) {
        score += event.getPoints() * multiplier;
    }

    public int getScore() {
        return score;
    }

    public int getMultiplier() {
        return multiplier;
    }

    @Override
    public void dispose() {
        eventBus.unsubscribe(ScoreEvent.class, scoreEventHandler);
        super.dispose();
    }
}
