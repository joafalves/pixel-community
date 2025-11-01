package org.pixel.demo.learning.decs.component;

import org.pixel.ext.decs.GameComponent;

public class ScoreValueComponent implements GameComponent {
    private int points;

    public ScoreValueComponent(int points) {
        this.points = points;
    }

    public int getPoints() {
        return points;
    }
}
