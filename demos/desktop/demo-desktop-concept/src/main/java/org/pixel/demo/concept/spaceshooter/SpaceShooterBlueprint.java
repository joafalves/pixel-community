package org.pixel.demo.concept.spaceshooter;

import org.pixel.blueprint.annotation.Blueprint;
import org.pixel.blueprint.annotation.Component;
import org.pixel.commons.event.EventManager;

@Blueprint
public class SpaceShooterBlueprint {

    @Component
    public EventManager eventManager() {
        return new EventManager();
    }
}
