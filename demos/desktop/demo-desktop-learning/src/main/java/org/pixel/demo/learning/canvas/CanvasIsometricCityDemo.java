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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class CanvasIsometricCityDemo extends DemoGame {

    private static final Logger log = LoggerFactory.getLogger(CanvasIsometricCityDemo.class);

    private static final int COLUMNS = 40;
    private static final int ROWS = 40;
    private static final int TILE_WIDTH = 64;
    private static final int TILE_HEIGHT = TILE_WIDTH / 2;

    private GLCanvasRenderer canvas;
    private ContentManager content;
    private SdfFont font;
    private Vector2 gridOffset;
    private Vector2 dragStartPos;
    private boolean dragging;

    private int[][] tileData;
    private List<Vehicle> vehicles;

    private final Color topFaceColor = new Color(0.8f, 0.8f, 0.8f, 1f);
    private final Color leftFaceColor = new Color(0.6f, 0.6f, 0.6f, 1f);
    private final Color rightFaceColor = new Color(0.4f, 0.4f, 0.4f, 1f);
    private final Color streetColor = new Color(0.2f, 0.2f, 0.2f, 1f);

    private static class Vehicle {
        float row, col;
        float speed = 1f;
        Vector2 direction;
        Color color;

        Vehicle(int row, int col, Color color) {
            this.row = row;
            this.col = col;
            this.color = color;
            this.direction = new Vector2(1, 0);
        }
    }

    public CanvasIsometricCityDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        super.load();

        int windowWidth = getWindowManager().getWindowWidth();
        int windowHeight = getWindowManager().getWindowHeight();
        canvas = new GLCanvasRenderer(windowWidth, windowHeight);

        content = ServiceProvider.get(ContentManager.class);

        font = content.load("fonts/roboto-regular.ttf", SdfFont.class,
                new FontImporterSettings(14, 3));

        gridOffset = new Vector2(windowWidth / 2.0f, windowHeight / 4.0f);
        dragStartPos = new Vector2();

        generateCityLayout();
        spawnVehicles();

        log.info("Drawing a city with CanvasRenderer");
    }

    private void generateCityLayout() {
        tileData = new int[ROWS * 2 + 1][COLUMNS * 2 + 1];
        Random random = new Random();

        for (int col = 0; col < COLUMNS * 2 + 1; col++) {
            tileData[ROWS][col] = -1;
        }

        for (int i = 0; i < 5; i++) {
            int streetCol = random.nextInt(COLUMNS * 2 + 1);
            for (int row = 0; row < ROWS * 2 + 1; row++) {
                tileData[row][streetCol] = -1;
            }
        }

        for (int row = 0; row < ROWS * 2 + 1; row++) {
            for (int col = 0; col < COLUMNS * 2 + 1; col++) {
                if (tileData[row][col] == -1) continue;

                boolean isNextToStreet = false;
                for (int r = -1; r <= 1; r++) {
                    for (int c = -1; c <= 1; c++) {
                        if (r == 0 && c == 0) continue;
                        int checkRow = row + r;
                        int checkCol = col + c;
                        if (checkRow >= 0 && checkRow < ROWS * 2 + 1 && checkCol >= 0 && checkCol < COLUMNS * 2 + 1) {
                            if (tileData[checkRow][checkCol] == -1) {
                                isNextToStreet = true;
                                break;
                            }
                        }
                    }
                    if (isNextToStreet) break;
                }

                if (isNextToStreet && random.nextFloat() < 0.4f) {
                    tileData[row][col] = random.nextInt(200) + 50;
                } else if (random.nextFloat() < 0.1f) {
                    tileData[row][col] = random.nextInt(100) + 20;
                }
            }
        }
    }

    private void spawnVehicles() {
        if (vehicles == null) {
            vehicles = new ArrayList<>();
        }
        Random random = new Random();
        for (int i = 0; i < 50 - vehicles.size(); i++) {
            int row = random.nextInt(ROWS * 2 + 1);
            int col = random.nextInt(COLUMNS * 2 + 1);
            if (tileData[row][col] == -1) {
                vehicles.add(new Vehicle(row - ROWS, col - COLUMNS, Color.random()));
            }
        }
    }

    @Override
    public void update(DeltaTime delta) {
        if (Mouse.isMouseButtonDown(MouseButton.LEFT)) {
            if (!dragging) {
                dragging = true;
                Mouse.getPosition(dragStartPos);
                getWindowManager().setWindowCursorType(WindowCursorType.HAND);
            } else {
                Vector2 currentMousePos = Mouse.getPosition();
                Vector2 dragDelta = Vector2.subtract(currentMousePos, dragStartPos);
                gridOffset.add(dragDelta);
                dragStartPos = currentMousePos;
            }
        } else {
            dragging = false;
            getWindowManager().setWindowCursorType(WindowCursorType.ARROW);
        }

        updateVehicles(delta);

        super.update(delta);
    }

    private void updateVehicles(DeltaTime delta) {
        List<Vehicle> toRemove = new ArrayList<>();
        for (Vehicle vehicle : vehicles) {
            vehicle.col += vehicle.direction.getX() * vehicle.speed * delta.getElapsed();
            vehicle.row += vehicle.direction.getY() * vehicle.speed * delta.getElapsed();

            int vehicleCol = Math.round(vehicle.col);
            int vehicleRow = Math.round(vehicle.row);

            if (vehicleCol < -COLUMNS || vehicleCol > COLUMNS || vehicleRow < -ROWS || vehicleRow > ROWS) {
                toRemove.add(vehicle);
                continue;
            }

            if (isIntersection(vehicleRow + ROWS, vehicleCol + COLUMNS)) {
                List<Vector2> possibleDirections = getPossibleDirections(vehicleRow + ROWS, vehicleCol + COLUMNS, vehicle.direction);
                if (!possibleDirections.isEmpty()) {
                    vehicle.direction = possibleDirections.get(new Random().nextInt(possibleDirections.size()));
                }
            } else {
                int nextCol = (int) (vehicle.col + vehicle.direction.getX());
                int nextRow = (int) (vehicle.row + vehicle.direction.getY());
                if (nextCol < -COLUMNS || nextCol > COLUMNS || nextRow < -ROWS || nextRow > ROWS || tileData[nextRow + ROWS][nextCol + COLUMNS] != -1) {
                    List<Vector2> possibleDirections = getPossibleDirections(vehicleRow + ROWS, vehicleCol + COLUMNS, vehicle.direction);
                    if (!possibleDirections.isEmpty()) {
                        vehicle.direction = possibleDirections.get(0);
                    } else { // Dead end
                        vehicle.direction.set(-vehicle.direction.getX(), -vehicle.direction.getY());
                    }
                }
            }
        }
        vehicles.removeAll(toRemove);
        if (vehicles.size() < 50) {
            spawnVehicles();
        }
    }

    private boolean isIntersection(int r, int c) {
        if (r < 0 || r >= ROWS * 2 + 1 || c < 0 || c >= COLUMNS * 2 + 1 || tileData[r][c] != -1) {
            return false;
        }
        int streetNeighbors = 0;
        if (r > 0 && tileData[r - 1][c] == -1) streetNeighbors++;
        if (r < ROWS * 2 && tileData[r + 1][c] == -1) streetNeighbors++;
        if (c > 0 && tileData[r][c - 1] == -1) streetNeighbors++;
        if (c < COLUMNS * 2 && tileData[r][c + 1] == -1) streetNeighbors++;
        return streetNeighbors > 2;
    }

    private List<Vector2> getPossibleDirections(int r, int c, Vector2 currentDirection) {
        List<Vector2> directions = new ArrayList<>();
        if (r > 0 && tileData[r - 1][c] == -1) directions.add(new Vector2(0, -1));
        if (r < ROWS * 2 && tileData[r + 1][c] == -1) directions.add(new Vector2(0, 1));
        if (c > 0 && tileData[r][c - 1] == -1) directions.add(new Vector2(-1, 0));
        if (c < COLUMNS * 2 && tileData[r][c + 1] == -1) directions.add(new Vector2(1, 0));

        if (directions.size() > 1) {
            return directions.stream().filter(dir -> dir.getX() != -currentDirection.getX() || dir.getY() != -currentDirection.getY()).collect(Collectors.toList());
        }
        return directions;
    }

    @Override
    public void draw(DeltaTime delta) {
        super.draw(delta);

        canvas.begin();
        canvas.save();
        canvas.translate(gridOffset.getX(), gridOffset.getY());

        for (int s = -ROWS - COLUMNS; s <= ROWS + COLUMNS; s++) {
            for (int row = -ROWS; row <= ROWS; row++) {
                int col = s - row;
                if (col < -COLUMNS || col > COLUMNS) {
                    continue;
                }

                int tileValue = tileData[row + ROWS][col + COLUMNS];
                drawTileObject(col, row, tileValue);

                for (Vehicle vehicle : vehicles) {
                    if (Math.round(vehicle.row) == row && Math.round(vehicle.col) == col) {
                        drawVehicle(vehicle);
                    }
                }
            }
        }

        canvas.restore();

        if (font != null) {
            String info = String.format("FPS: %d | Drag to move", getSmoothedFps());
            canvas.drawText(info, font, 10, 10, Color.WHITE);
        }

        canvas.end();
    }

    private void drawTileObject(int col, int row, int data) {
        float isoX = (col - row) * TILE_WIDTH;
        float isoY = (col + row) * TILE_HEIGHT;

        if (data == -1) {
            drawStreet(isoX, isoY);
        } else if (data > 0) {
            drawBuilding(isoX, isoY, data);
        } else {
            drawTile(isoX, isoY, Color.DARK_GRAY);
        }
    }

    private void drawBuilding(float isoX, float isoY, int height) {
        drawTile(isoX, isoY, Color.DARK_GRAY);

        float topY = isoY - height;
        canvas.beginPath();
        canvas.moveTo(isoX, topY - TILE_HEIGHT);
        canvas.lineTo(isoX + TILE_WIDTH, topY);
        canvas.lineTo(isoX, topY + TILE_HEIGHT);
        canvas.lineTo(isoX - TILE_WIDTH, topY);
        canvas.closePath();
        canvas.fill(topFaceColor);

        canvas.beginPath();
        canvas.moveTo(isoX - TILE_WIDTH, isoY);
        canvas.lineTo(isoX, isoY + TILE_HEIGHT);
        canvas.lineTo(isoX, topY + TILE_HEIGHT);
        canvas.lineTo(isoX - TILE_WIDTH, topY);
        canvas.closePath();
        canvas.fill(leftFaceColor);

        canvas.beginPath();
        canvas.moveTo(isoX + TILE_WIDTH, isoY);
        canvas.lineTo(isoX, isoY + TILE_HEIGHT);
        canvas.lineTo(isoX, topY + TILE_HEIGHT);
        canvas.lineTo(isoX + TILE_WIDTH, topY);
        canvas.closePath();
        canvas.fill(rightFaceColor);
    }

    private void drawStreet(float isoX, float isoY) {
        canvas.beginPath();
        canvas.moveTo(isoX, isoY - TILE_HEIGHT);
        canvas.lineTo(isoX + TILE_WIDTH, isoY);
        canvas.lineTo(isoX, isoY + TILE_HEIGHT);
        canvas.lineTo(isoX - TILE_WIDTH, isoY);
        canvas.closePath();
        canvas.fill(streetColor);
    }

    private void drawTile(float isoX, float isoY, Color color) {
        float topX = isoX;
        float topY = isoY - TILE_HEIGHT;
        float rightX = isoX + TILE_WIDTH;
        float rightY = isoY;
        float bottomX = isoX;
        float bottomY = isoY + TILE_HEIGHT;
        float leftX = isoX - TILE_WIDTH;
        float leftY = isoY;

        canvas.strokeLine(topX, topY, rightX, rightY, 1, color);
        canvas.strokeLine(rightX, rightY, bottomX, bottomY, 1, color);
        canvas.strokeLine(bottomX, bottomY, leftX, leftY, 1, color);
        canvas.strokeLine(leftX, leftY, topX, topY, 1, color);
    }

    private void drawVehicle(Vehicle vehicle) {
        float isoX = (vehicle.col - vehicle.row) * TILE_WIDTH;
        float isoY = (vehicle.col + vehicle.row) * TILE_HEIGHT;
        float vehicleSize = TILE_WIDTH / 4f;

        canvas.beginPath();
        canvas.moveTo(isoX, isoY - vehicleSize / 2f);
        canvas.lineTo(isoX + vehicleSize, isoY);
        canvas.lineTo(isoX, isoY + vehicleSize / 2f);
        canvas.lineTo(isoX - vehicleSize, isoY);
        canvas.closePath();
        canvas.fill(vehicle.color);
    }

    @Override
    public void dispose() {
        if (canvas != null) {
            canvas.dispose();
        }
        super.dispose();
    }

    public static void main(String[] args) {
        var settings = new WindowSettings(1920, 1080);
        settings.setDevMode(true);
        settings.setWindowResizable(true);
        settings.setMultisampling(2);
        settings.setVsync(false);
        settings.setBackgroundColor(Color.BLACK);
        settings.setTitle("Isometric City Demo");

        var window = new CanvasIsometricCityDemo(settings);
        window.start();
    }
}