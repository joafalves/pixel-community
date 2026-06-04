package org.pixel.demo.learning.blueprintadvanced.blueprint;

import org.pixel.blueprint.annotation.Auto;
import org.pixel.blueprint.annotation.Blueprint;
import org.pixel.blueprint.annotation.Component;
import org.pixel.blueprint.annotation.Conditional;
import org.pixel.content.Texture;
import org.pixel.demo.learning.blueprintadvanced.effect.DebugGridEffect;
import org.pixel.demo.learning.blueprintadvanced.effect.FloatingOrbsEffect;
import org.pixel.demo.learning.blueprintadvanced.effect.PulseWaveEffect;
import org.pixel.demo.learning.blueprintadvanced.effect.StarfieldEffect;
import org.pixel.demo.learning.blueprintadvanced.effect.VisualEffect;

@Blueprint
public class EffectBlueprint {

    private static final float WIDTH = 800f;
    private static final float HEIGHT = 600f;

    @Component(value = "starfield", tags = {"visual-effect", "layer:background"})
    public VisualEffect starfield(@Auto("starTexture") Texture texture) {
        return new StarfieldEffect(texture, WIDTH, HEIGHT);
    }

    @Component(value = "floatingOrbs", tags = {"visual-effect", "layer:foreground"})
    public VisualEffect floatingOrbs(
            @Auto("orbRedTexture") Texture red,
            @Auto("orbBlueTexture") Texture blue) {
        return new FloatingOrbsEffect(red, blue, WIDTH, HEIGHT);
    }

    @Component(value = "pulseWave", tags = {"visual-effect", "layer:special"})
    public VisualEffect pulseWave(@Auto("pulseTexture") Texture texture) {
        return new PulseWaveEffect(texture, WIDTH / 2, HEIGHT / 2);
    }

    @Component(value = "debugGrid", tags = {"visual-effect", "layer:debug"})
    @Conditional({DevModeCondition.class})
    public VisualEffect debugGrid(@Auto("gridTexture") Texture texture) {
        return new DebugGridEffect(texture, WIDTH, HEIGHT);
    }
}
