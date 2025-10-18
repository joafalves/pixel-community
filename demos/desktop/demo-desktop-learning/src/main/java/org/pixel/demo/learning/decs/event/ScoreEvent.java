package org.pixel.demo.learning.decs.event;

public class ScoreEvent {
    private final int points;
    private final float x;
    private final float y;

    public ScoreEvent(int points, float x, float y) {
        this.points = points;
        this.x = x;
        this.y = y;
    }

    public int getPoints() {
        return points;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
