package org.pixel.demo.learning.decs.system;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.service.ServiceProvider;
import org.pixel.content.importer.settings.FontImporterSettings;
import org.pixel.core.Camera2D;
import org.pixel.demo.learning.decs.component.ItemGameComponent;
import org.pixel.demo.learning.decs.component.PlayerGameComponent;
import org.pixel.demo.learning.decs.component.PositionGameComponent;
import org.pixel.ext.decs.GameGroup;
import org.pixel.ext.decs.GameSystem;
import org.pixel.ext.decs.GameWorld;
import org.pixel.graphics.render.canvas.CanvasRenderer;
import org.pixel.graphics.render.canvas.GLCanvasRenderer;
import org.pixel.graphics.render.canvas.TextStyle;
import org.pixel.graphics.render.canvas.text.SdfFont;

/**
 * A fancy rendering system using CanvasRenderer.
 * Draws entities as circles with text labels.
 */
public class CanvasRenderGameSystem extends GameSystem {

    private final CanvasRenderer canvas;
    private final GameWorld world;
    private Camera2D camera;
    private GameGroup players;
    private GameGroup items;
    private SdfFont font;

    public CanvasRenderGameSystem(GameWorld world) {
        this.world = world;
        this.canvas = world.getData().get(GLCanvasRenderer.class);
    }

    @Override
    public void load() {
        this.camera = world.getData().get(Camera2D.class);
        this.players = world.getGroup(PlayerGameComponent.class, PositionGameComponent.class);
        this.items = world.getGroup(ItemGameComponent.class, PositionGameComponent.class);

        // Load a font for labels
        this.font = ServiceProvider.get(org.pixel.content.ContentManager.class)
                .load("fonts/roboto-regular.ttf", SdfFont.class, new FontImporterSettings(16, 1));
    }

    @Override
    public void draw(DeltaTime delta) {
        // Begin with camera's view matrix for world-space rendering
        canvas.begin(camera.getViewMatrix());

        // Draw items as green circles with "Item" label below
        for (var entity : items) {
            var position = world.getComponent(entity, PositionGameComponent.class);
            float x = position.getPosition().getX() + 16; // Center of 32x32 entity
            float y = position.getPosition().getY() + 16;

            // Draw green filled circle
            canvas.fillCircle(x, y, 16, Color.GREEN);

            // Draw "Item" text below
            TextStyle labelStyle = new TextStyle(Color.WHITE);
            canvas.drawText("Item", font, x - 15, y + 25, labelStyle);
        }

        // Draw player as red circle with "Player" label above
        for (var entity : players) {
            var position = world.getComponent(entity, PositionGameComponent.class);
            float x = position.getPosition().getX() + 16; // Center of 32x32 entity
            float y = position.getPosition().getY() + 16;

            // Draw red filled circle
            canvas.fillCircle(x, y, 16, Color.RED);

            // Draw "Player" text above
            TextStyle labelStyle = new TextStyle(new Color(0, 1f, 1f)); // Cyan
            canvas.drawText("Player", font, x - 20, y - 25, labelStyle);
        }

        canvas.end();
    }
}
