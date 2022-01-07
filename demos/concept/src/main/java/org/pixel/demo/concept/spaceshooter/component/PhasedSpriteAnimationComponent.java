package org.pixel.demo.concept.spaceshooter.component;

import org.pixel.ext.ecs.component.SpriteAnimationComponent;

public class PhasedSpriteAnimationComponent extends SpriteAnimationComponent {



    public PhasedSpriteAnimationComponent(int rows, int columns, float delay) {
        super(rows, columns, delay);
    }
}
