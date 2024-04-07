package org.pixel.demo.concept.roadcar;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.demo.concept.roadcar.model.RoadSegment;
import org.pixel.demo.concept.roadcar.model.Vehicle;
import org.pixel.ext.log4j.Log4j2LoggerStrategy;
import org.pixel.graphics.WindowSettings;
import org.pixel.graphics.Game;
import org.pixel.graphics.render.RenderEngine2D;
import org.pixel.graphics.render.nanovg.NvgRenderEngine;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.keyboard.KeyboardKey;
import org.pixel.math.MathHelper;
import org.pixel.math.SizeInt;
import org.pixel.math.Vector2;

import java.util.ArrayList;
import java.util.List;

public class RoadCarGame extends Game {

    static {
        LoggerFactory.setDefaultStrategy(new Log4j2LoggerStrategy());
    }

    private static final float CAMERA_SPEED = 330;
    private static final int VEHICLE_SPAWN_DELAY = 1000;

    protected final List<Vehicle> vehicleList = new ArrayList<>();
    protected final List<RoadSegment> roadSegments = new ArrayList<>();

    private RenderEngine2D renderEngine;
    private Vector2 cameraPosition;
    private float cameraZoom;
    private float spawnElapsed = 0f;

    public RoadCarGame(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        super.load();

        cameraPosition = new Vector2(150, 150);
        cameraZoom = 0.75f;
        renderEngine = new NvgRenderEngine(getSettings().getWindowWidth(), getSettings().getWindowHeight());

        roadSegments.add(new RoadSegment(new Vector2(0, 0), true));
        roadSegments.add(new RoadSegment(new Vector2(200, 0)));
        roadSegments.get(roadSegments.size() - 2).addNextSegment(roadSegments.get(roadSegments.size() - 1));
        roadSegments.add(new RoadSegment(new Vector2(400, 0)));
        roadSegments.get(roadSegments.size() - 2).addNextSegment(roadSegments.get(roadSegments.size() - 1));
        roadSegments.add(new RoadSegment(new Vector2(600, 0)));
        roadSegments.get(roadSegments.size() - 2).addNextSegment(roadSegments.get(roadSegments.size() - 1));

        roadSegments.add(new RoadSegment(new Vector2(700, 200)));
        roadSegments.get(roadSegments.size() - 2).addNextSegment(roadSegments.get(roadSegments.size() - 1));
        roadSegments.add(new RoadSegment(new Vector2(700, 400)));
        roadSegments.get(roadSegments.size() - 2).addNextSegment(roadSegments.get(roadSegments.size() - 1));

        roadSegments.add(new RoadSegment(new Vector2(800, -200)));
        roadSegments.get(roadSegments.size() - 4).addNextSegment(roadSegments.get(roadSegments.size() - 1));
        roadSegments.add(new RoadSegment(new Vector2(800, -300)));
        roadSegments.get(roadSegments.size() - 2).addNextSegment(roadSegments.get(roadSegments.size() - 1));
        roadSegments.add(new RoadSegment(new Vector2(600, -400)));
        roadSegments.get(roadSegments.size() - 2).addNextSegment(roadSegments.get(roadSegments.size() - 1));
        roadSegments.add(new RoadSegment(new Vector2(400, -400)));
        roadSegments.get(roadSegments.size() - 2).addNextSegment(roadSegments.get(roadSegments.size() - 1));
        roadSegments.add(new RoadSegment(new Vector2(200, -400)));
        roadSegments.get(roadSegments.size() - 2).addNextSegment(roadSegments.get(roadSegments.size() - 1));
    }

    @Override
    public void update(DeltaTime delta) {
        handleInput(delta);
        handleVehicleSpawn(delta);
        handleVehicleMovement(delta);
        removeIdleVehicles();
    }

    private void handleInput(DeltaTime delta) {
        if (Keyboard.isKeyDown(KeyboardKey.D) || Keyboard.isKeyDown(KeyboardKey.RIGHT)) {
            cameraPosition.add(-CAMERA_SPEED * delta.getElapsed(), 0);
        } else if (Keyboard.isKeyDown(KeyboardKey.A) || Keyboard.isKeyDown(KeyboardKey.LEFT)) {
            cameraPosition.add(CAMERA_SPEED * delta.getElapsed(), 0);
        }

        if (Keyboard.isKeyDown(KeyboardKey.W) || Keyboard.isKeyDown(KeyboardKey.UP)) {
            cameraPosition.add(0, CAMERA_SPEED * delta.getElapsed());
        } else if (Keyboard.isKeyDown(KeyboardKey.S) || Keyboard.isKeyDown(KeyboardKey.DOWN)) {
            cameraPosition.add(0, -CAMERA_SPEED * delta.getElapsed());
        }

        if (Keyboard.isKeyDown(KeyboardKey.Q)) {
            cameraZoom -= delta.getElapsed() * 2f;
        } else if (Keyboard.isKeyDown(KeyboardKey.E)) {
            cameraZoom += delta.getElapsed() * 2f;
        }
    }

    private void handleVehicleSpawn(DeltaTime delta) {
        spawnElapsed += delta.getElapsedMs();
        if (spawnElapsed >= VEHICLE_SPAWN_DELAY) {
            spawnElapsed = 0;

            var segment = getNearestRoadSegmentStarter(cameraPosition);
            var targetSegment = segment.getRandomNextSegment();
            var vehicle = Vehicle.builder()
                    .sizeInt(new SizeInt(75, 40))
                    .color(Color.random())
                    .position(new Vector2(segment.getPosition()))
                    .orientation(MathHelper.direction(segment.getPosition(), targetSegment.getPosition()))
                    .targetRoadSegment(targetSegment)
                    .build();

            vehicleList.add(vehicle);
        }
    }

    private void handleVehicleMovement(DeltaTime delta) {
        for (Vehicle vehicle : vehicleList) {
            if (vehicle.getTargetRoadSegment() == null) {
                continue;
            }

            float direction = MathHelper.direction(vehicle.getPosition(), vehicle.getTargetRoadSegment().getPosition());
            if (Math.abs(vehicle.getOrientation() - direction) >= MathHelper.PI) {
                direction += (MathHelper.PI2 * (vehicle.getOrientation() < 0 ? -1 : 1));
            }

            vehicle.setOrientation(MathHelper.linearInterpolation(
                    vehicle.getOrientation(), direction, 3f * delta.getElapsed())
            );
            vehicle.getPosition().add(
                    MathHelper.cos(vehicle.getOrientation()) * 200f * delta.getElapsed(),
                    MathHelper.sin(vehicle.getOrientation()) * 200f * delta.getElapsed()
            );

            handleVehicleCollision(delta, vehicle);
        }
    }

    private void handleVehicleCollision(DeltaTime delta, Vehicle vehicle) {
        var targetSegment = vehicle.getTargetRoadSegment();
        if (vehicle.getPosition().distanceTo(targetSegment.getPosition()) <= 40) {
            vehicle.setTargetRoadSegment(targetSegment.getRandomNextSegment());
        }
    }

    private void removeIdleVehicles() {
        for (int i = vehicleList.size() - 1; i >= 0; i--) {
            if (vehicleList.get(i).getTargetRoadSegment() == null) {
                vehicleList.remove(i);
            }
        }
    }

    @Override
    public void draw(DeltaTime delta) {
        renderEngine.begin();
        renderEngine.translate(cameraPosition);
        renderEngine.scale(cameraZoom, cameraZoom);

        drawRoadSegments();
        drawVehicles();

        renderEngine.end();
    }

    private void drawRoadSegments() {
        for (RoadSegment roadSegment : roadSegments) {
            renderEngine.push();

            renderEngine.translate(roadSegment.getPosition());
            renderEngine.beginPath();
            renderEngine.circle(0, 0, 4);
            renderEngine.fillColor(Color.GREEN);
            renderEngine.fill();
            renderEngine.endPath();

            renderEngine.pop();
        }
    }

    private void drawVehicles() {
        for (Vehicle vehicle : vehicleList) {
            renderEngine.push();

            renderEngine.translate(vehicle.getPosition());
            renderEngine.rotate(vehicle.getOrientation());

            renderEngine.beginPath();
            renderEngine.rectangle(
                    -vehicle.getSizeInt().getHalfWidth(), -vehicle.getSizeInt().getHalfHeight(),
                    vehicle.getSizeInt().getWidth(), vehicle.getSizeInt().getHeight());
            renderEngine.fillColor(vehicle.getColor());
            renderEngine.fill();
            renderEngine.endPath();

            renderEngine.beginPath();
            renderEngine.moveTo(0, 0);
            renderEngine.lineTo(vehicle.getSizeInt().getHalfWidth(), 0);
            renderEngine.strokeColor(Color.BLACK);
            renderEngine.stroke();
            renderEngine.endPath();

            renderEngine.pop();
        }
    }


    private RoadSegment getNearestRoadSegmentStarter(Vector2 position) {
        RoadSegment nearestRoadSegment = null;
        float nearestDistance = Float.MAX_VALUE;
        for (RoadSegment roadSegment : roadSegments) {
            if (!roadSegment.isStarter()) {
                continue;
            }

            float distance = Vector2.distance(roadSegment.getPosition(), position);
            if (distance < nearestDistance) {
                nearestRoadSegment = roadSegment;
                nearestDistance = distance;
            }
        }

        return nearestRoadSegment;
    }

    public static void main(String[] args) {
        final int width = 800;
        final int height = 600;
        var settings = new WindowSettings(width / 2, height / 2);
        settings.setWindowResizable(false);
        settings.setMultisampling(2);
        settings.setVsync(true);
        settings.setDebugMode(false);
        settings.setWindowWidth(width);
        settings.setWindowHeight(height);

        var window = new RoadCarGame(settings);
        window.start();
    }
}
