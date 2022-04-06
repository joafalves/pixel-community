package org.pixel.demo.concept.spaceshooter.entity;

import org.pixel.content.TextureFrame;
import org.pixel.ext.ecs.Sprite;

public class SpaceShipSprite extends Sprite {

    private final int totalHp;
    private int hp;

    public SpaceShipSprite(String name, TextureFrame frame, int totalHp) {
        super(name, frame);
        this.totalHp = totalHp;
        this.hp = totalHp;
    }

    public boolean isDestroyed() {
        return hp <= 0;
    }

    public void damage(int amount) {
        hp -= amount;
    }

    public int getTotalHp() {
        return totalHp;
    }

    public int getHp() {
        return hp;
    }
}
