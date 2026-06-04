package org.pixel.demo.learning.blueprintadvanced;

import org.pixel.blueprint.BlueprintRepository;
import org.pixel.blueprint.annotation.Auto;
import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.commons.service.ServiceProvider;
import org.pixel.content.ContentManager;
import org.pixel.content.Font;
import org.pixel.content.importer.settings.FontImporterSettings;
import org.pixel.core.Camera2D;
import org.pixel.core.Game;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.blueprintadvanced.effect.VisualEffect;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.keyboard.KeyboardKey;
import org.pixel.math.Vector2;

import java.util.List;

public class BlueprintAdvancedDemo extends Game {

    private static final Logger log = LoggerFactory.getLogger(BlueprintAdvancedDemo.class);

    @Auto
    private SpriteBatch spriteBatch;

    @Auto
    private Camera2D mainCamera;

    @Auto(tags = {"visual-effect"})
    private List<VisualEffect> allEffects;

    @Auto(tags = {"layer:background"})
    private List<VisualEffect> backgroundLayer;

    @Auto(tags = {"layer:foreground"})
    private List<VisualEffect> foregroundLayer;

    @Auto(tags = {"layer:special"})
    private List<VisualEffect> specialLayer;

    @Auto(tags = {"layer:debug"})
    private List<VisualEffect> debugLayer;

    private ContentManager contentManager;
    private Font font;
    private Font smallFont;

    public BlueprintAdvancedDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        super.load();

        contentManager = ServiceProvider.get(ContentManager.class);
        font = contentManager.loadFont("fonts/gidole-regular.ttf", new FontImporterSettings(36, 1));
        smallFont = contentManager.loadFont("fonts/gidole-regular.ttf", new FontImporterSettings(16, 1));

        log.info("Blueprint Particle Lab loaded!");
        log.info("Effects discovered by tag layer:");
        log.info("  Background layer: {0}", backgroundLayer.size());
        log.info("  Foreground layer: {0}", foregroundLayer.size());
        log.info("  Special layer: {0}", specialLayer.size());
        log.info("  Debug layer: {0}{1}  {2}",
                debugLayer.size(),
                debugLayer.isEmpty() ? "" : " (dev mode)",
                debugLayer.isEmpty() ? "(only visible in dev mode)" : "");
        log.info("  Total effects: {0}", allEffects.size());
    }

    @Override
    public void update(DeltaTime delta) {
        super.update(delta);

        if (Keyboard.isKeyPressed(KeyboardKey.ESCAPE)) {
            dispose();
        }

        for (VisualEffect effect : allEffects) {
            effect.update(delta);
        }
    }

    @Override
    public void draw(DeltaTime delta) {
        super.draw(delta);

        spriteBatch.begin(mainCamera.getViewMatrix());

        // Draw layers in correct render order (tags determine draw order!)
        for (VisualEffect effect : backgroundLayer) {
            effect.draw(spriteBatch);
        }
        for (VisualEffect effect : foregroundLayer) {
            effect.draw(spriteBatch);
        }
        for (VisualEffect effect : specialLayer) {
            effect.draw(spriteBatch);
        }
        for (VisualEffect effect : debugLayer) {
            effect.draw(spriteBatch);
        }

        drawHud();

        spriteBatch.end();
    }

    private void drawHud() {
        spriteBatch.drawText(font, "Blueprint Particle Lab", new Vector2(200, 30), Color.WHITE, 36);

        float y = 570;
        spriteBatch.drawText(smallFont,
                String.format("Layers: bg=%d | fg=%d | special=%d | debug=%s",
                        backgroundLayer.size(),
                        foregroundLayer.size(),
                        specialLayer.size(),
                        debugLayer.isEmpty() ? "off" : "on"),
                new Vector2(10, y), Color.CORAL, 16);

        spriteBatch.drawText(smallFont, "ESC to quit", new Vector2(720, y), Color.WHITE, 16);
    }

    @Override
    public void dispose() {
        if (font != null) font.dispose();
        if (smallFont != null) smallFont.dispose();
        if (contentManager != null) contentManager.dispose();
        super.dispose();
    }

    public static void main(String[] args) {
        var settings = new WindowSettings(800, 600);
        settings.setTitle("Blueprint Particle Lab");
        settings.setVsync(true);
        settings.setDevMode(false);
        settings.setBackgroundColor(new Color(0x0f0f1aff));
        settings.setBlueprintPackages(new String[]{"org.pixel.demo.learning.blueprintadvanced"});

        BlueprintRepository.getDefault().registerComponent(WindowSettings.class, settings, "appSettings");

        var game = new BlueprintAdvancedDemo(settings);
        game.start();
    }
}
