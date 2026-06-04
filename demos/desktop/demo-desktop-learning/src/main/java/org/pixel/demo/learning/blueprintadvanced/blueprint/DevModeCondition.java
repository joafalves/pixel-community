package org.pixel.demo.learning.blueprintadvanced.blueprint;

import org.pixel.blueprint.BlueprintRepository;
import org.pixel.blueprint.annotation.Condition;
import org.pixel.core.WindowSettings;

public class DevModeCondition implements Condition {
    @Override
    public boolean matches() {
        WindowSettings settings = BlueprintRepository.getDefault().uget(WindowSettings.class, "appSettings");
        return settings != null && settings.isDevMode();
    }
}
