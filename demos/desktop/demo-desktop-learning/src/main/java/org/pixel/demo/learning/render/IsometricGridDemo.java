package org.pixel.demo.learning.render;

import org.pixel.commons.DeltaTime;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.core.WindowCursorType;
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

    private static final int COLUMNS = 20;
    private static final int ROWS = 20;
    private static final int TILE_WIDTH = 32;
    private static final int TILE_HEIGHT = TILE_WIDTH / 2;

    private RenderEngine2D re;
    private Vector2 gridOffset; // To keep track of the grid offset due to dragging
    private Vector2 dragStartPos; // Initial drag position
    private boolean dragging; // Whether the mouse is dragging
    private Vector2 highlightedTile; // The tile currently being highlighted

    public IsometricGridDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        int windowWidth = getWindowManager().getWindowWidth();
        int windowHeight = getWindowManager().getWindowHeight();

        re = new NvgRenderEngine(windowWidth, windowHeight);
        gridOffset = new Vector2(windowWidth / 2.0f, windowHeight / 2.0f); // Start with no offset
        dragStartPos = new Vector2();
        highlightedTile = new Vector2(-1, -1); // Initialize with an invalid tile index

        log.info("Drawing {} lines.", (COLUMNS * 2 + 1) * (ROWS * 2 + 1));
    }

    @Override
    public void update(DeltaTime delta) {
        if (Mouse.isMouseButtonDown(MouseButton.LEFT)) {
            if (!dragging) {
                dragging = true;
                Mouse.getPosition(dragStartPos);
                getWindowManager().setWindowCursorType(WindowCursorType.HAND);
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
            getWindowManager().setWindowCursorType(WindowCursorType.ARROW);
        }

        // Calculate which tile is highlighted
        Vector2 mousePos = Mouse.getPosition();
        float localX = mousePos.getX() - gridOffset.getX();
        float localY = mousePos.getY() - gridOffset.getY();

        float col = (localX / TILE_WIDTH + localY / TILE_HEIGHT) / 2;
        float row = (localY / TILE_HEIGHT - localX / TILE_WIDTH) / 2;

        highlightedTile.set(Math.round(col), Math.round(row));
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

                // Highlight the current tile
                if (highlightedTile.getX() == col && highlightedTile.getY() == row) {
                    re.beginPath();
                    re.moveTo(isoX, isoY - TILE_HEIGHT);
                    re.lineTo(isoX + TILE_WIDTH, isoY);
                    re.lineTo(isoX, isoY + TILE_HEIGHT);
                    re.lineTo(isoX - TILE_WIDTH, isoY);
                    re.endPath();
                    re.fillColor(Color.WHITE);
                    re.fill();
                }

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
        var settings = new WindowSettings(1280, 720);
        settings.setDebugMode(true);
        settings.setWindowResizable(true);
        settings.setMultisampling(2);
        settings.setVsync(true);
        settings.setBackgroundColor(Color.BLACK);
        settings.setWindowWidth(1920);
        settings.setWindowHeight(1080);
        settings.setTitle("Isometric Grid Demo - Drag mouse to move grid");

        var window = new IsometricGridDemo(settings);
        window.start();
    }
}
