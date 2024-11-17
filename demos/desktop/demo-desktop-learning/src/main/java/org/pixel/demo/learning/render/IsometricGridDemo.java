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
    private static final int COLUMNS = 16;
    private static final int ROWS = 16;
    private static final int TILE_WIDTH = 32;
    private static final int TILE_HEIGHT = TILE_WIDTH / 2;

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
        // Begin rendering
        re.begin();
        re.translate(gridOffset.getX(), gridOffset.getY());

        // Offset grid origin based on startX and startY
        for (int row = -ROWS; row <= ROWS; row++) {
            for (int col = -COLUMNS; col <= COLUMNS; col++) {
                float isoX = (col - row) * TILE_WIDTH;
                float isoY = (col + row) * TILE_HEIGHT;

                // Draw diamond-shaped tile
                re.beginPath();
                re.moveTo(isoX, isoY - TILE_HEIGHT);
                re.lineTo(isoX + TILE_WIDTH, isoY);
                re.lineTo(isoX, isoY + TILE_HEIGHT);
                re.lineTo(isoX - TILE_WIDTH, isoY);
                re.endPath();
                re.strokeColor(Color.DARK_GRAY);
                re.stroke();
            }
        }

        // Draw grid lines
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
