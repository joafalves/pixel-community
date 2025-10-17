package org.pixel.demo.learning.canvas;

import org.pixel.commons.DeltaTime;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.commons.service.ServiceProvider;
import org.pixel.content.ContentManager;
import org.pixel.content.importer.settings.FontImporterSettings;
import org.pixel.core.WindowCursorType;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.commons.Color;
import org.pixel.graphics.render.canvas.GLCanvasRenderer;
import org.pixel.graphics.render.canvas.text.SdfFont;
import org.pixel.input.mouse.Mouse;
import org.pixel.input.mouse.MouseButton;
import org.pixel.math.Vector2;

public class CanvasIsometricGridDemo extends DemoGame {

    private static final Logger log = LoggerFactory.getLogger(CanvasIsometricGridDemo.class);

    private static final int COLUMNS = 20;
    private static final int ROWS = 20;
    private static final int TILE_WIDTH = 32;
    private static final int TILE_HEIGHT = TILE_WIDTH / 2;

    private GLCanvasRenderer canvas;
    private ContentManager content;
    private SdfFont font;
    private Vector2 gridOffset; // To keep track of the grid offset due to dragging
    private Vector2 dragStartPos; // Initial drag position
    private boolean dragging; // Whether the mouse is dragging
    private Vector2 highlightedTile; // The tile currently being highlighted

    public CanvasIsometricGridDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        super.load();

        // Create canvas renderer with window dimensions for screen-space rendering
        int windowWidth = getWindowManager().getWindowWidth();
        int windowHeight = getWindowManager().getWindowHeight();
        canvas = new GLCanvasRenderer(windowWidth, windowHeight);

        // Initialize content manager
        content = ServiceProvider.get(ContentManager.class);

        // Load font for UI text
        font = content.load("fonts/roboto-regular.ttf", SdfFont.class,
                new FontImporterSettings(14, 3));

        gridOffset = new Vector2(windowWidth / 2.0f, windowHeight / 2.0f); // Start centered
        dragStartPos = new Vector2();
        highlightedTile = new Vector2(-1, -1); // Initialize with an invalid tile index

        log.info("Drawing {0} tiles.", (COLUMNS * 2 + 1) * (ROWS * 2 + 1));
        log.info("Using CanvasRenderer with new line drawing API");
        log.info("Window size: {0}x{1}", windowWidth, windowHeight);
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

        super.update(delta);
    }

    @Override
    public void draw(DeltaTime delta) {
        super.draw(delta);

        // Begin rendering in screen-space (no camera transformation)
        canvas.begin();

        // Apply grid offset using transform
        canvas.save();
        canvas.translate(gridOffset.getX(), gridOffset.getY());

        // Draw isometric grid tiles
        for (int row = -ROWS; row <= ROWS; row++) {
            for (int col = -COLUMNS; col <= COLUMNS; col++) {
                float isoX = (col - row) * TILE_WIDTH;
                float isoY = (col + row) * TILE_HEIGHT;

                // Calculate the four corners of the diamond
                float topX = isoX;
                float topY = isoY - TILE_HEIGHT;
                float rightX = isoX + TILE_WIDTH;
                float rightY = isoY;
                float bottomX = isoX;
                float bottomY = isoY + TILE_HEIGHT;
                float leftX = isoX - TILE_WIDTH;
                float leftY = isoY;

                // Highlight the current tile with filled diamond using Path API
                if (highlightedTile.getX() == col && highlightedTile.getY() == row) {
                    // Draw filled semi-transparent diamond using Path API (HTML5 Canvas-style!)
                    canvas.beginPath();
                    canvas.moveTo(topX, topY);
                    canvas.lineTo(rightX, rightY);
                    canvas.lineTo(bottomX, bottomY);
                    canvas.lineTo(leftX, leftY);
                    canvas.closePath();
                    canvas.fill(new Color(1, 1, 1, 0.5f)); // Semi-transparent white fill
                    
                    // Draw bright outline around highlighted tile
                    canvas.strokeLine(topX, topY, rightX, rightY, 2, Color.WHITE);
                    canvas.strokeLine(rightX, rightY, bottomX, bottomY, 2, Color.WHITE);
                    canvas.strokeLine(bottomX, bottomY, leftX, leftY, 2, Color.WHITE);
                    canvas.strokeLine(leftX, leftY, topX, topY, 2, Color.WHITE);
                } else {
                    // Draw diamond-shaped tile outline (only for non-highlighted tiles to save draw calls)
                    canvas.strokeLine(topX, topY, rightX, rightY, 1, Color.DARK_GRAY);
                    canvas.strokeLine(rightX, rightY, bottomX, bottomY, 1, Color.DARK_GRAY);
                    canvas.strokeLine(bottomX, bottomY, leftX, leftY, 1, Color.DARK_GRAY);
                    canvas.strokeLine(leftX, leftY, topX, topY, 1, Color.DARK_GRAY);
                }
            }
        }

        canvas.restore();

        // Draw UI overlay
        if (font != null) {
            String info = String.format("FPS: %d | Tiles: %d | Drag to move | Tile: (%.0f, %.0f)",
                    getSmoothedFps(),
                    (COLUMNS * 2 + 1) * (ROWS * 2 + 1),
                    highlightedTile.getX(),
                    highlightedTile.getY());
            canvas.drawText(info, font, 10, 10, Color.WHITE);
        }

        canvas.end();
    }

    @Override
    public void dispose() {
        if (canvas != null) {
            canvas.dispose();
        }
        super.dispose();
    }

    public static void main(String[] args) {
        var settings = new WindowSettings(1280, 720);
        settings.setDevMode(true);
        settings.setWindowResizable(true);
        settings.setMultisampling(2);
        settings.setVsync(false);
        settings.setBackgroundColor(Color.BLACK);
        settings.setWindowWidth(1920);
        settings.setWindowHeight(1080);
        settings.setTitle("Isometric Grid Demo - Drag mouse to move grid");

        var window = new CanvasIsometricGridDemo(settings);
        window.start();
    }
}
