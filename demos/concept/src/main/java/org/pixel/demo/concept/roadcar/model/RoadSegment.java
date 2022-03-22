package org.pixel.demo.concept.roadcar.model;

import java.util.ArrayList;
import java.util.List;
import org.pixel.math.Vector2;

public class RoadSegment {
    private final boolean isStarter;
    private final Vector2 position;
    private List<RoadSegment> nextSegments;

    public RoadSegment(Vector2 position) {
        this(position, false);
    }

    public RoadSegment(Vector2 position, boolean isStarter) {
        this.position = position;
        this.isStarter = isStarter;
    }

    public void addNextSegment(RoadSegment segment) {
        if (nextSegments == null) {
            nextSegments = new ArrayList<>();
        }

        nextSegments.add(segment);
    }

    public RoadSegment getRandomNextSegment() {
        if (nextSegments == null) {
            return null;
        }

        return nextSegments.get((int) (Math.random() * nextSegments.size()));
    }

    public List<RoadSegment> getNextSegments() {
        return nextSegments;
    }

    public Vector2 getPosition() {
        return position;
    }

    public boolean isStarter() {
        return isStarter;
    }
}
