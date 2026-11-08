package org.pixel.demo.concept.spaceshooter.world;

import org.pixel.demo.concept.spaceshooter.GameConfig;

/**
 * How enemies move and fire, per {@link Movement}. Stateless: everything lives in the {@link Enemy}.
 */
final class EnemyAi {

    private static final float TWO_PI = (float) (Math.PI * 2);

    private EnemyAi() {
    }

    static void update(Enemy e, World world, float dt) {
        e.age += dt;
        e.stateTime += dt;
        e.flash = Math.max(0f, e.flash - dt * 18f); // a short blink: under constant fire it flickers
        switch (e.movement) {
            case STRAIGHT -> e.x -= e.speed * dt;
            case SINE -> {
                e.x -= e.speed * dt;
                e.y = e.baseY + e.amplitude * (float) Math.sin(e.age * e.frequency + e.phase);
            }
            case DIVE -> dive(e, world, dt);
            case SWOOP -> {
                e.x -= e.speed * dt;
                e.y = e.baseY + e.amplitude * (float) Math.sin(e.age * e.frequency);
            }
            case HOLD -> raider(e, world, dt);
            case BOSS -> boss(e, world, dt);
        }
        if (e.armed && e.x < GameConfig.WIDTH - 24 && e.x > 80) {
            e.fireTimer -= dt;
            if (e.fireTimer <= 0f) {
                e.armed = false;
                world.fireAimed(e.x - 6, e.y, 92f + 4f * world.level(), 0, 0f);
            }
        }
        if (e.active && e.age > 1f && (e.x < -40f || e.x > GameConfig.WIDTH + 90f || e.y < -70f
                || e.y > GameConfig.HEIGHT + 70f)) {
            world.escape(e);
        }
    }

    private static void dive(Enemy e, World world, float dt) {
        if (e.state == 0) {
            e.x -= e.speed * dt;
            if (e.x < e.holdX) {
                e.setState(1);
            }
            return;
        }
        Player p = world.player;
        float targetY = p.alive ? p.y : e.y;
        e.vy += clamp((targetY - e.y) * 5f, -300f, 300f) * dt;
        e.vy = clamp(e.vy, -100f, 100f);
        e.speed = Math.min(230f, e.speed + 150f * dt);
        e.x -= e.speed * dt;
        e.y += e.vy * dt;
    }

    private static void raider(Enemy e, World world, float dt) {
        switch (e.state) {
            case 0 -> { // fly in
                e.x += (e.holdX - e.x) * Math.min(1f, 2.6f * dt);
                if (Math.abs(e.x - e.holdX) < 1.5f) {
                    e.setState(1);
                    e.fireTimer = 0.7f;
                }
            }
            case 1 -> { // strafe and fire bursts
                e.y = e.baseY + e.amplitude * (float) Math.sin(e.stateTime * e.frequency);
                e.x = e.holdX + 4f * (float) Math.sin(e.stateTime * 1.3f);
                e.fireTimer -= dt;
                if (e.fireTimer <= 0f) {
                    world.fireAimed(e.x - 14, e.y, 112f + 5f * world.level(), 3, 0.17f);
                    e.fireTimer = Math.max(1.0f, 2.1f - 0.08f * world.level());
                }
                if (e.stateTime > e.stay) {
                    e.setState(2);
                    e.speed = 0f;
                }
            }
            default -> { // leave to the left, accelerating
                e.speed = Math.min(260f, e.speed + 150f * dt);
                e.x -= e.speed * dt;
            }
        }
    }

    private static void boss(Enemy e, World world, float dt) {
        if (e.dying) {
            e.fireTimer -= dt;
            if (e.fireTimer <= 0f) {
                e.fireTimer = 0.1f;
                world.bossBlast(e);
            }
            if (e.stateTime > 2.6f) {
                world.finishBoss(e);
            }
            return;
        }
        if (e.state == 0) { // enter
            e.x += (e.holdX - e.x) * Math.min(1f, 1.8f * dt);
            if (Math.abs(e.x - e.holdX) < 3f) {
                e.setState(1);
                e.fireTimer = 1.2f;
                e.fireTimer2 = 0.6f;
                e.summonTimer = 5f;
            }
            return;
        }
        e.y = e.baseY + 48f * (float) Math.sin(e.stateTime * 0.55f);
        e.x = e.holdX + 10f * (float) Math.sin(e.stateTime * 0.31f);

        float health = e.healthFraction();
        int phase = health > 0.66f ? 1 : health > 0.33f ? 2 : 3;
        if (phase > e.bossPhase) {
            e.bossPhase = phase;
            e.fireTimer = 1.0f;
            e.fireTimer2 = 0.5f;
            world.bossPhaseChanged(e);
        }
        float level = world.level();
        float noseX = e.x - 40f;
        e.fireTimer -= dt;
        e.fireTimer2 -= dt;
        switch (phase) {
            case 1 -> {
                if (e.fireTimer <= 0f) { // a fan aimed at the player
                    world.fireAimed(noseX, e.y, 110f + 3f * level, 5, 0.14f);
                    e.fireTimer = 1.5f;
                }
                if (e.fireTimer2 <= 0f) { // the wing guns
                    world.fireEnemyShot(e.x - 18f, e.y - 32f, (float) Math.PI, 150f);
                    world.fireEnemyShot(e.x - 18f, e.y + 32f, (float) Math.PI, 150f);
                    e.fireTimer2 = 0.7f;
                }
            }
            case 2 -> {
                e.angle += 2.1f * dt;
                if (e.fireTimer2 <= 0f) { // a two-armed spiral
                    world.fireEnemyShot(e.x - 8f, e.y, e.angle, 86f);
                    world.fireEnemyShot(e.x - 8f, e.y, e.angle + (float) Math.PI, 86f);
                    e.fireTimer2 = 0.09f;
                }
                if (e.fireTimer <= 0f) {
                    world.fireAimed(noseX, e.y, 118f + 3f * level, 7, 0.12f);
                    e.fireTimer = 2.2f;
                }
            }
            default -> {
                e.angle -= 2.6f * dt;
                if (e.fireTimer2 <= 0f) { // a three-armed spiral, the other way
                    for (int arm = 0; arm < 3; arm++) {
                        world.fireEnemyShot(e.x - 8f, e.y, e.angle + arm * TWO_PI / 3f, 92f);
                    }
                    e.fireTimer2 = 0.12f;
                }
                if (e.fireTimer <= 0f) {
                    world.fireAimed(noseX, e.y, 126f + 3f * level, 7, 0.11f);
                    e.fireTimer = 1.4f;
                }
                e.summonTimer -= dt;
                if (e.summonTimer <= 0f) { // escorts
                    world.summonDarts(e);
                    e.summonTimer = 4.5f;
                }
            }
        }
    }

    static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
