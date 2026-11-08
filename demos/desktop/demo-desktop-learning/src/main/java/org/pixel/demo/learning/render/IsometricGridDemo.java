package org.pixel.demo.learning.render;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.core.DesktopLauncher;
import org.pixel.core.DesktopWindowManager;
import org.pixel.core.WindowCursorType;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.graphics.canvas.Canvas;
import org.pixel.graphics.draw.Layer;
import org.pixel.graphics.pass.RenderPipeline;
import org.pixel.input.mouse.Mouse;
import org.pixel.input.mouse.MouseButton;
import org.pixel.math.Vector2;

/**
 * An isometric grid drawn with {@link Canvas}: the tile edges are long lines along the two isometric axes (84 lines
 * instead of 1,681 outlined diamonds), and the tile under the cursor is a filled path. Drag with the left mouse
 * button to move the grid.
 */
public class IsometricGridDemo extends DemoGame {

    private static final Logger log = LoggerFactory.getLogger(IsometricGridDemo.class);

    private static final int COLUMNS = 20;
    private static final int ROWS = 20;
    private static final int TILE_WIDTH = 32;
    private static final int TILE_HEIGHT = TILE_WIDTH / 2;
    private static final Color GRID = new Color(0.33f, 0.33f, 0.36f, 1f);

    private static final Layer MAIN = Layer.ordered("main");

    private RenderPipeline pipeline;
    private final Vector2 gridOffset = new Vector2();
    private final Vector2 dragStart = new Vector2();
    private final Vector2 mouse = new Vector2();
    private boolean dragging;
    private int highlightedCol = Integer.MIN_VALUE;
    private int highlightedRow = Integer.MIN_VALUE;

    @Override
    public void load() {
        super.load();
        gameCamera.setOrigin(Vector2.zero());
        pipeline = RenderPipeline.builder(graphics().getRenderBackend()).pass(gameCamera, MAIN).build();
        gridOffset.set(gameCamera.getWidth() / 2f, gameCamera.getHeight() / 2f);
        log.info("{0} tiles, drawn as {1} lines.", (COLUMNS * 2 + 1) * (ROWS * 2 + 1),
                (COLUMNS * 2 + 2) + (ROWS * 2 + 2));
    }

    @Override
    public void update(DeltaTime delta) {
        Mouse.getVirtualPosition(mouse);
        DesktopWindowManager window = DesktopWindowManager.of(context());
        if (Mouse.isMouseButtonDown(MouseButton.LEFT)) {
            if (!dragging) {
                dragging = true;
                window.setWindowCursorType(WindowCursorType.HAND);
            } else {
                gridOffset.add(mouse.getX() - dragStart.getX(), mouse.getY() - dragStart.getY());
            }
            dragStart.set(mouse);
        } else if (dragging) {
            dragging = false;
            window.setWindowCursorType(WindowCursorType.ARROW);
        }

        // tile under the cursor (inverse of the isometric projection below)
        float localX = mouse.getX() - gridOffset.getX();
        float localY = mouse.getY() - gridOffset.getY();
        highlightedCol = Math.round((localX / TILE_WIDTH + localY / TILE_HEIGHT) / 2);
        highlightedRow = Math.round((localY / TILE_HEIGHT - localX / TILE_WIDTH) / 2);
    }

    @Override
    public void draw(DeltaTime delta) {
        Canvas c = pipeline.canvas(MAIN);
        c.translate(gridOffset.getX(), gridOffset.getY());

        // highlighted tile below the grid lines
        if (Math.abs(highlightedCol) <= COLUMNS && Math.abs(highlightedRow) <= ROWS) {
            float isoX = isoX(highlightedCol, highlightedRow), isoY = isoY(highlightedCol, highlightedRow);
            c.path().moveTo(isoX, isoY - TILE_HEIGHT).lineTo(isoX + TILE_WIDTH, isoY)
                    .lineTo(isoX, isoY + TILE_HEIGHT).lineTo(isoX - TILE_WIDTH, isoY).close()
                    .fill(Color.WHITE);
        }

        // tile edges lie on half-integer rows and columns: one line per edge across the whole grid
        float first = -COLUMNS - 0.5f, last = COLUMNS + 0.5f;
        for (int k = -ROWS; k <= ROWS + 1; k++) {
            float row = k - 0.5f;
            c.line(isoX(first, row), isoY(first, row), isoX(last, row), isoY(last, row)).width(1).color(GRID);
        }
        first = -ROWS - 0.5f;
        last = ROWS + 0.5f;
        for (int k = -COLUMNS; k <= COLUMNS + 1; k++) {
            float col = k - 0.5f;
            c.line(isoX(col, first), isoY(col, first), isoX(col, last), isoY(col, last)).width(1).color(GRID);
        }
        pipeline.render();
    }

    private static float isoX(float col, float row) {
        return (col - row) * TILE_WIDTH;
    }

    private static float isoY(float col, float row) {
        return (col + row) * TILE_HEIGHT;
    }

    @Override
    public void dispose() {
        pipeline.dispose();
        super.dispose();
    }

    public static void main(String[] args) {
        var settings = new WindowSettings(1280, 720);
        settings.setDevMode(true);
        settings.setWindowResizable(true);
        settings.setVsync(true);
        settings.setBackgroundColor(Color.BLACK);
        settings.setWindowWidth(1920);
        settings.setWindowHeight(1080);
        settings.setTitle("Isometric Grid Demo - Drag mouse to move grid");

        DesktopLauncher.run(new IsometricGridDemo(), settings);
    }
}
