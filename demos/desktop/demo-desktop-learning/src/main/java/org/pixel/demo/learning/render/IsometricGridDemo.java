package org.pixel.demo.learning.render;

import org.pixel.commons.DeltaTime;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.commons.Color;
import org.pixel.graphics.render.RenderEngine2D;
import org.pixel.graphics.render.nanovg.NvgRenderEngine;
import org.pixel.input.mouse.Mouse;
import org.pixel.input.mouse.MouseButton;
import org.pixel.math.Vector2;


public class IsometricGridDemo extends DemoGame {

    private static final Logger log = LoggerFactory.getLogger(IsometricGridDemo.class);

    private static final float TILE_SIZE = 48; // Size of the diamond tiles

    private RenderEngine2D re;
    private Vector2 gridOffset; // To keep track of the grid offset due to dragging
    private Vector2 dragStartPos; // Initial drag position
    private boolean dragging; // Whether the mouse is dragging

    public IsometricGridDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        re = new NvgRenderEngine(getSettings().getWindowWidth(), getSettings().getWindowHeight());
        gridOffset = new Vector2(0, 0); // Start with no offset
    }

    @Override
    public void update(DeltaTime delta) {
        if (Mouse.isMouseButtonDown(MouseButton.LEFT)) {
            if (!dragging) {
                dragging = true;
                dragStartPos = Mouse.getPosition();
                log.info("Drag start: " + dragStartPos);
            } else {
                Vector2 currentMousePos = Mouse.getPosition();
                Vector2 dragDelta = Vector2.subtract(currentMousePos, dragStartPos);
                gridOffset.add(dragDelta);
                dragStartPos = currentMousePos;
            }
        } else {
            if (dragging) {
                log.info("Drag end");
            }
            dragging = false;
        }
    }

    @Override
    public void draw(DeltaTime delta) {
        re.begin();
        re.translate(gridOffset.getX(), gridOffset.getY());

        // Calculate visible grid bounds
        float halfTileWidth = TILE_SIZE / 2;
        float halfTileHeight = TILE_SIZE / 4;

        int cols = (int) (getWindowManager().getWindowWidth() / TILE_SIZE) + 3;
        int rows = (int) (getWindowManager().getWindowHeight() / TILE_SIZE) + 3;

        // Offset grid origin based on startX and startY
        for (int row = -rows; row <= rows; row++) {
            for (int col = -cols; col <= cols; col++) {
                float isoX = (col - row) * halfTileWidth;
                float isoY = (col + row) * halfTileHeight;

                // Draw diamond-shaped tile
                re.beginPath();
                re.moveTo(isoX, isoY - halfTileHeight);
                re.lineTo(isoX + halfTileWidth, isoY);
                re.lineTo(isoX, isoY + halfTileHeight);
                re.lineTo(isoX - halfTileWidth, isoY);
                re.endPath();
                re.strokeColor(Color.DARK_GRAY);
                re.stroke();
            }
        }

        re.end();
    }

    @Override
    public void dispose() {
        re.dispose();
        super.dispose();
    }

    public static void main(String[] args) {
        var settings = new WindowSettings(800, 640);
        settings.setWindowResizable(true);
        settings.setMultisampling(2);
        settings.setVsync(true);
        settings.setBackgroundColor(Color.BLACK);

        var window = new IsometricGridDemo(settings);
        window.start();
    }

}
