package org.pixel.tiled.content;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Layer {
    private final int width;
    private final int height;
    private final double offsetX;
    private final double offsetY;
    private final int tiles[][];

    public Layer(int width, int height, double offsetX, double offsetY){
        this.width = width;
        this.height = height;
        this.offsetX = offsetX;
        this.offsetY = offsetY;

        tiles = new int[height][width];
    }

    public double getOffsetX() {
        return offsetX;
    }

    public double getOffsetY() {
        return offsetY;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int[][] getTiles() {
        return tiles;
    }

    public void addTile(int x, int y, int gID) {
        tiles[y][x] = gID;
    }
}
