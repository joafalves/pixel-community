package org.pixel.demo.learning.decs.component;

import org.pixel.ext.decs.GameComponent;
import org.pixel.ext.decs.GameEntity;

public class AIComponent implements GameComponent {
    public enum AIType {
        CHASER,  // Follows player
        SHOOTER, // Stays at distance and shoots
        DASHER   // Quick dash attacks
    }

    private AIType type;
    private GameEntity target;
    private float thinkTimer;
    private float actionCooldown;

    public AIComponent(AIType type) {
        this.type = type;
        this.thinkTimer = 0;
        this.actionCooldown = 0;
    }

    public AIType getType() {
        return type;
    }

    public GameEntity getTarget() {
        return target;
    }

    public void setTarget(GameEntity target) {
        this.target = target;
    }

    public float getThinkTimer() {
        return thinkTimer;
    }

    public void setThinkTimer(float thinkTimer) {
        this.thinkTimer = thinkTimer;
    }

    public void updateThinkTimer(float delta) {
        if (thinkTimer > 0) {
            thinkTimer -= delta;
        }
    }

    public float getActionCooldown() {
        return actionCooldown;
    }

    public void setActionCooldown(float actionCooldown) {
        this.actionCooldown = actionCooldown;
    }

    public void updateActionCooldown(float delta) {
        if (actionCooldown > 0) {
            actionCooldown -= delta;
        }
    }

    public boolean canAct() {
        return actionCooldown <= 0;
    }
}
