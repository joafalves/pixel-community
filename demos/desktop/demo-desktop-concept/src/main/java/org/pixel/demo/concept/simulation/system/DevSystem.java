package org.pixel.demo.concept.simulation.system;

import lombok.RequiredArgsConstructor;
import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.content.ContentManager;
import org.pixel.content.importer.settings.FontImporterSettings;
import org.pixel.core.Game;
import org.pixel.commons.event.EventBus;
import org.pixel.demo.concept.simulation.dictionary.ResourceDictionary;
import org.pixel.demo.concept.simulation.events.WindowSizeChanged;
import org.pixel.ext.decs.GameSystem;
import org.pixel.ext.decs.GameWorld;
import org.pixel.graphics.render.canvas.Canvas;
import org.pixel.graphics.render.canvas.GLCanvas;
import org.pixel.graphics.render.canvas.text.SdfFont;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.keyboard.KeyboardKey;

@RequiredArgsConstructor
public class DevSystem extends GameSystem {

    private final GameWorld world;

    private Game game;
    private Canvas canvas;

    private SdfFont debugFont;

    @Override
    public void load() {
        game = world.getData().get(Game.class);
        canvas = new GLCanvas(game.getWindowManager().getWindowWidth(), game.getWindowManager().getWindowHeight());

        final var eventBus = world.getData().optional(EventBus.class)
                .orElseThrow(() -> new IllegalStateException("EventBus not found in world data"));
        final var contentManager = world.getData().optional(ContentManager.class)
                .orElseThrow(() -> new IllegalStateException("ContentManager not found in world data"));

        debugFont = contentManager.load("font/" + ResourceDictionary.DEBUG_FONT, SdfFont.class,
                FontImporterSettings.builder()
                        .fontSize(14)
                        .build());

        // Event subscriptions
        eventBus.subscribe(WindowSizeChanged.class, this::onViewportChanged);
    }

    private void onViewportChanged(WindowSizeChanged event) {
        canvas.setViewport(event.width(), event.height());
    }

    @Override
    public void update(DeltaTime delta) {
        if (Keyboard.isKeyPressed(KeyboardKey.ESCAPE)) {
            world.getData().optional(Game.class)
                    .ifPresent(Game::dispose);
        }
    }

    @Override
    public void draw(DeltaTime delta) {
        canvas.begin();

        canvas.text("FPS: " + game.getFps(), debugFont, 10, 10)
                .withFill(Color.WHITE)
                .withStroke(Color.BLACK, 2)
                .apply();

        canvas.rect(4, 4, game.getViewportWidth() - 8, game.getViewportHeight() - 8)
                .withRoundedCorners(4)
                .withStroke(2, Color.RED)
                .apply();

        canvas.end();
    }

    @Override
    public void dispose() {
        canvas.dispose();
        super.dispose();
    }
}
