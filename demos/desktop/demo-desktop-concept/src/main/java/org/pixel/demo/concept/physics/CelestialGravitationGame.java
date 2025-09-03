package org.pixel.demo.concept.physics;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.core.Game;
import org.pixel.core.WindowSettings;
import org.pixel.graphics.render.RenderEngine2D;
import org.pixel.graphics.render.nanovg.NvgRenderEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CelestialGravitationGame extends Game {

    // Gravitational constant G (N * m^2 / kg^2) - now using double for precision
    private static final double G = 6.67430e-11;
    // Simulation speed - now using double for precision
    private static final double SIMULATION_SPEED = 1000000.0;

    private final List<PhysicsBody> bodies = new ArrayList<>();

    private RenderEngine2D renderEngine;
    private float displayScale; // Pixels per meter for rendering (can remain float)

    /**
     * Constructor
     *
     * @param settings The game settings.
     */
    public CelestialGravitationGame(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        // Initialize the game entities and components
        renderEngine = new NvgRenderEngine(getSettings().getWindowWidth(), getSettings().getWindowHeight());

        // Sun
        double sunMass = 1.989e30; // kg - now using double
        bodies.add(PhysicsBody.builder()
                .mass(sunMass) // Mass of the Sun in kg
                .position(Vector2D.zero())
                .velocity(Vector2D.zero())
                .build());

        // Earth
        double earthMass = 5.972e24; // kg - now using double
        double earthOrbitRadius = 1.496e11; // meters (1 AU) - now using double
        // Calculate orbital velocity for Earth (assuming circular orbit around a much more massive Sun)
        // Calculations now use double, then cast to float for Vector2
        double earthOrbitalVelocity = Math.sqrt(G * sunMass / earthOrbitRadius);
        bodies.add(PhysicsBody.builder()
                .mass(earthMass) // Mass of the Earth in kg
                .position(new Vector2D(earthOrbitRadius, 0)) // Position in meters (1 AU) - cast to float
                .velocity(new Vector2D(0, earthOrbitalVelocity)) // Velocity in m/s - cast to float
                .build());

        // Calculate a display scale to fit the orbit within the window
        // We want to fit roughly 2 AU across half the window width, so 1 AU is about 300 pixels
        displayScale = 300f / (float) earthOrbitRadius; // Cast earthOrbitRadius to float for division
    }

    @Override
    public void update(DeltaTime delta) {
        // Calculate the simulation time step based on real elapsed time and simulation speed
        double deltaTimeSimulation = delta.getElapsed() * SIMULATION_SPEED; // Use double for deltaTimeSimulation

        // Map to store the net force acting on each body in the current time step
        Map<PhysicsBody, Vector2D> netForces = new HashMap<>();
        for (PhysicsBody body : bodies) {
            netForces.put(body, Vector2D.zero()); // Initialize net force to zero for each body
        }

        // Calculate gravitational forces between all unique pairs of bodies
        for (int i = 0; i < bodies.size(); i++) {
            PhysicsBody bodyA = bodies.get(i);
            for (int j = i + 1; j < bodies.size(); j++) { // Iterate through unique pairs (i.e., avoid A-A and A-B then B-A)
                PhysicsBody bodyB = bodies.get(j);

                // Vector from bodyA to bodyB
                Vector2D r_vector = Vector2D.subtract( bodyB.getPosition(), bodyA.getPosition());
                // Use double for distanceSq calculation to maintain precision
                double distanceSq = r_vector.lengthSquared(); // Vector2.magnitudeSq() returns float, but we need double for precision here

                System.out.println("Distance squared between bodies: " + distanceSq);

                // Prevent division by zero if bodies are at the same position (e.g., collision)
                if (distanceSq < 1e-10) { // Use double literal
                    distanceSq = 1e-10; // Set a tiny minimum distance
                }

                // Calculate gravitational force magnitude - now using double for precision
                double forceMagnitude = (G * bodyA.getMass() * bodyB.getMass()) / distanceSq;

                // Calculate force vector (direction * magnitude)
                // Force on bodyA from bodyB - cast forceMagnitude to float for Vector2.multiply
                Vector2D forceOnA = Vector2D.multiply(Vector2D.normalize(r_vector), (float) forceMagnitude);
                // Force on bodyB from bodyA (equal and opposite)
                Vector2D forceOnB = Vector2D.multiply(forceOnA, -1);

                // Accumulate net forces for each body
                netForces.get(bodyA).add(forceOnA);
                netForces.get(bodyB).add(forceOnB);
            }
        }

        // Apply net forces and update positions for all bodies
        for (PhysicsBody body : bodies) {
            // Apply the calculated net force to update the body's velocity
            applyForce(body, netForces.get(body), (float) deltaTimeSimulation); // Cast deltaTimeSimulation to float
            // Update the body's position based on its new velocity
            updatePosition(body, (float) deltaTimeSimulation); // Cast deltaTimeSimulation to float
        }
    }

    @Override
    public void draw(DeltaTime delta) {
        renderEngine.begin();
        // Translates the render engine to the center of the window, so that the origin (0,0) is at the center
        renderEngine.translate(
                getSettings().getWindowWidth() / 2f,
                getSettings().getWindowHeight() / 2f
        );

        for (var body : bodies) {
            renderEngine.beginPath();
            // Scale the body's position from meters to pixels for rendering
            float sx = (float) (body.getPosition().getX() * displayScale);
            float sy = (float) (body.getPosition().getY() * displayScale);

            // Determine visual radius: larger for the Sun, smaller for planets
            float visualRadius = 10f; // Default small radius for planets
            if (body.getMass() > 1e29) { // If mass is comparable to a star (e.g., Sun) - use double literal for comparison
                visualRadius = 30f; // Larger radius for stars
            }

            renderEngine.circle(sx, sy, visualRadius); // Draw a circle for each body
            renderEngine.fill();
            renderEngine.endPath();
        }

        renderEngine.end();
    }

    /**
     * Applies a force to a physics body, updating its velocity based on acceleration.
     * @param body The physics body to apply the force to.
     * @param force The force vector applied to the body.
     * @param deltaTime The time step for the simulation.
     */
    private void applyForce(PhysicsBody body, Vector2D force, float deltaTime) {
        // Calculate the acceleration from the force and the mass of the body (a = F / m)
        // Divide force by body's mass (double), then cast result to float for Vector2
        final var acceleration = Vector2D.divide(force, body.getMass());
        // Update the velocity of the body (v = v + a * dt)
        body.getVelocity().add(Vector2D.multiply(acceleration, deltaTime));
    }

    /**
     * Updates the position of a physics body based on its current velocity.
     * @param body The physics body to update.
     * @param deltaTime The time step for the simulation.
     */
    private void updatePosition(PhysicsBody body, float deltaTime) {
        // Update the position of the body (p = p + v * dt)
        body.getPosition().add(Vector2D.multiply(body.getVelocity(), deltaTime));
    }


    public static void main(String[] args) {
        final var screenWidth = 1280;
        final var screenHeight = 720;
        final var settings = new WindowSettings(screenWidth, screenHeight);
        settings.setWindowResizable(false);
        settings.setMultisampling(2);
        settings.setVsync(true);
        settings.setDevMode(false);
        settings.setWindowWidth(screenWidth);
        settings.setWindowHeight(screenHeight);
        settings.setBackgroundColor(Color.BLACK);

        final var window = new CelestialGravitationGame(settings);
        window.start();
    }
}
