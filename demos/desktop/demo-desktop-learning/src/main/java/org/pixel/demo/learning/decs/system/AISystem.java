package org.pixel.demo.learning.decs.system;

import org.pixel.commons.DeltaTime;
import org.pixel.demo.learning.decs.component.*;
import org.pixel.ext.decs.GameEntity;
import org.pixel.ext.decs.GameGroup;
import org.pixel.ext.decs.GameSystem;
import org.pixel.ext.decs.GameWorld;
import org.pixel.math.Vector2;

public class AISystem extends GameSystem {

    private final GameWorld world;
    private GameGroup enemies;
    private GameGroup players;

    public AISystem(GameWorld world) {
        this.world = world;
    }

    @Override
    public void load() {
        enemies = world.getGroup(AIComponent.class, PositionGameComponent.class, VelocityGameComponent.class);
        players = world.getGroup(PlayerGameComponent.class, PositionGameComponent.class);
    }

    @Override
    public void update(DeltaTime delta) {
        // Find player target (assumes single player)
        GameEntity player = null;
        for (var p : players) {
            player = p;
            break;
        }

        if (player == null) return;

        for (var enemy : enemies) {
            var ai = world.getComponent(enemy, AIComponent.class);
            var position = world.getComponent(enemy, PositionGameComponent.class);
            var velocity = world.getComponent(enemy, VelocityGameComponent.class);

            ai.setTarget(player);
            ai.updateThinkTimer(delta.getElapsed());
            ai.updateActionCooldown(delta.getElapsed());

            var playerPos = world.getComponent(player, PositionGameComponent.class);
            Vector2 toPlayer = new Vector2(
                    playerPos.getPosition().getX() - position.getPosition().getX(),
                    playerPos.getPosition().getY() - position.getPosition().getY()
            );
            float distance = toPlayer.length();

            switch (ai.getType()) {
                case CHASER:
                    // Move towards player
                    if (distance > 5) {
                        toPlayer.normalize();
                        toPlayer.multiply(100f); // Chase speed
                        velocity.getVelocity().set(toPlayer.getX(), toPlayer.getY());
                    } else {
                        velocity.getVelocity().set(0, 0);
                    }
                    break;

                case SHOOTER:
                    // Stay at distance 150-200, shoot when in range
                    if (distance < 150) {
                        // Move away
                        toPlayer.normalize();
                        toPlayer.multiply(-80f);
                        velocity.getVelocity().set(toPlayer.getX(), toPlayer.getY());
                    } else if (distance > 200) {
                        // Move closer
                        toPlayer.normalize();
                        toPlayer.multiply(60f);
                        velocity.getVelocity().set(toPlayer.getX(), toPlayer.getY());
                    } else {
                        // In range - stop and shoot
                        velocity.getVelocity().set(0, 0);
                        if (ai.canAct()) {
                            ai.setActionCooldown(1.5f); // Shoot every 1.5 seconds
                        }
                    }
                    break;

                case DASHER:
                    // Dash towards player periodically
                    if (ai.getThinkTimer() <= 0) {
                        toPlayer.normalize();
                        toPlayer.multiply(250f); // Fast dash
                        velocity.getVelocity().set(toPlayer.getX(), toPlayer.getY());
                        ai.setThinkTimer(0.5f); // Dash for 0.5 seconds
                        ai.setActionCooldown(2.0f); // Wait 2 seconds before next dash
                    } else {
                        velocity.getVelocity().multiply(0.95f); // Decelerate
                    }
                    break;
            }
        }
    }
}
