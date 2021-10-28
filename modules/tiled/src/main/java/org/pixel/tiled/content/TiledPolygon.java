package org.pixel.tiled.content;

import org.pixel.math.Vector2;

import java.util.List;

public class TiledPolygon extends TiledObject {
    private List<Vector2> vertices;

    public List<Vector2> getVertices() {
        return vertices;
    }

    public void setVertices(List<Vector2> vertices) {
        this.vertices = vertices;
    }
}
