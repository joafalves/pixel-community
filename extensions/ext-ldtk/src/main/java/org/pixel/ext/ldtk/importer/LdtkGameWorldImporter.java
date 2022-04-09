package org.pixel.ext.ldtk.importer;

import io.github.joafalves.ldtk.LdtkConverter;
import io.github.joafalves.ldtk.model.*;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.commons.util.TextUtils;
import org.pixel.content.*;
import org.pixel.ext.ldtk.*;
import org.pixel.ext.ldtk.LdtkGameIntLayer.LayerCoordinate;
import org.pixel.graphics.Color;
import org.pixel.math.MathHelper;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@ContentImporterInfo(type = LdtkGameWorld.class, extension = {".json", ".ldtk"})
public class LdtkGameWorldImporter implements ContentImporter<LdtkGameWorld> {

    private static final Logger log = LoggerFactory.getLogger(LdtkGameWorldImporter.class);

    @Override
    public LdtkGameWorld process(ImportContext ctx) {
        try {
            String jsonString = TextUtils.convertBufferToString(ctx.getBuffer());
            return processData(LdtkConverter.fromJsonString(jsonString), ctx.getContentManager());

        } catch (IOException e) {
            log.error("Failed to process LDTK world from file: '{}'.", ctx.getFilepath(), e);
        }

        return null;
    }

    /**
     * Creates a new world based on the given LDTK Project.
     *
     * @param coordinate     The LDTK project coordinate.
     * @param contentManager The content manager to load the assets.
     * @return The new world.
     */
    private LdtkGameWorld processData(Coordinate coordinate, ContentManager contentManager) {
        HashMap<String, LdtkGameLevel> levels = new HashMap<>();
        for (Level level : coordinate.getLevels()) {
            log.trace("Processing level: '{}'.", level.getIdentifier());

            LdtkGameLevel.LdtkGameLevelBuilder gameLevelBuilder = LdtkGameLevel.builder();
            gameLevelBuilder.identifier(level.getIdentifier());
            gameLevelBuilder.worldPosition(new Vector2(level.getWorldX(), level.getWorldY()));

            processLevelBackground(gameLevelBuilder, level, contentManager); // background
            processLevelLayers(gameLevelBuilder, level, contentManager); // layers

            levels.put(level.getIdentifier(), gameLevelBuilder.build());
        }

        return LdtkGameWorld.builder()
                .levelMap(levels)
                .build();
    }

    /**
     * Processes the background of the level.
     *
     * @param builder        The builder to add the background to.
     * @param level          The level to process.
     * @param contentManager The content manager to load the assets.
     */
    private void processLevelBackground(LdtkGameLevel.LdtkGameLevelBuilder builder, Level level,
                                        ContentManager contentManager) {

        Texture backgroundTexture = null;
        if (level.getBgRelPath() != null) {
            backgroundTexture = contentManager.loadTexture(level.getBgRelPath());
        }

        Rectangle backgroundCropArea = null;
        Rectangle backgroundDisplayArea = null;
        if (backgroundTexture != null && level.getBgPos() != null) {
            var bgPos = level.getBgPos();
            backgroundCropArea = new Rectangle((float) bgPos.getCropRect()[0],
                    (float) bgPos.getCropRect()[1], (float) bgPos.getCropRect()[2],
                    (float) bgPos.getCropRect()[3]);
            backgroundDisplayArea = new Rectangle(
                    (float) (level.getWorldX() + bgPos.getTopLeftPx()[0]),
                    (float) (level.getWorldY() + bgPos.getTopLeftPx()[1]),
                    (float) (backgroundTexture.getWidth() * bgPos.getScale()[0]),
                    (float) (backgroundTexture.getHeight() * bgPos.getScale()[1]));
        }

        builder.backgroundColor(Color.fromString(level.getBgColor()))
                .backgroundTexture(backgroundTexture)
                .backgroundDisplayArea(backgroundDisplayArea)
                .backgroundCropArea(backgroundCropArea);
    }

    /**
     * Processes the layers of the level.
     *
     * @param builder        The builder to add the layers to.
     * @param level          The level to process.
     * @param contentManager The content manager to load the assets.
     */
    private void processLevelLayers(LdtkGameLevel.LdtkGameLevelBuilder builder, Level level,
                                    ContentManager contentManager) {
        if (level.getLayerInstances() == null) {
            return; // no layers
        }

        List<LdtkGameLayer> layerList = new ArrayList<>();
        List<LdtkGameEntity> entityList = new ArrayList<>();
        for (LayerInstance layerInstance : level.getLayerInstances()) {
            var layer = layerInstance.getType().equalsIgnoreCase("IntGrid")
                    ? new LdtkGameIntLayer() : new LdtkGameLayer();
            layer.setVisible(layerInstance.getVisible());
            layer.setIdentifier(layerInstance.getIdentifier());
            layer.setGridWidth((int) layerInstance.getCWid());
            layer.setGridHeight((int) layerInstance.getCHei());
            layer.setGridSize((int) layerInstance.getGridSize());

            // tiles
            List<TileInstance> tileList = new ArrayList<>(); // to grab all visible tiles
            List<LdtkGameLayerTile> ldtkGameLayerTileList = new ArrayList<>();
            if (layerInstance.getGridTiles() != null && layerInstance.getGridTiles().length > 0) {
                tileList.addAll(List.of(layerInstance.getGridTiles())); // get grid-tiles instances
            }
            if (layerInstance.getAutoLayerTiles() != null && layerInstance.getAutoLayerTiles().length > 0) {
                tileList.addAll(List.of(layerInstance.getAutoLayerTiles())); // get auto-tiles instances
            }
            for (TileInstance gridTile : tileList) { // convert data tiles to game tiles
                ldtkGameLayerTileList.add(processTileInstance(gridTile, layerInstance, level));
            }
            layer.setTileList(ldtkGameLayerTileList);

            // texture
            if (layerInstance.getTilesetRelPath() != null) {
                layer.setTilesetTexture(contentManager.loadTexture(layerInstance.getTilesetRelPath()));
            }

            // "IntGrid" specific:
            if (layer instanceof LdtkGameIntLayer && layerInstance.getIntGrid() != null
                    && layerInstance.getIntGrid().length > 0) {
                var intLayer = (LdtkGameIntLayer) layer;
                var intGrid = layerInstance.getIntGrid();

                var coordinateList = new ArrayList<LayerCoordinate>();
                for (IntGridValueInstance intGridValueInstance : intGrid) {
                    var gridY = (int) MathHelper.floor(
                            intGridValueInstance.getCoordId() / (float) layerInstance.getCWid());
                    var gridX = (int) (intGridValueInstance.getCoordId() - gridY * layerInstance.getCWid());

                    coordinateList.add(new LayerCoordinate((int) intGridValueInstance.getCoordId(), gridX, gridY, 1));
                }

                intLayer.setLayerCoordinateList(coordinateList);
            }

            layerList.add(layer);

            // entities (attached directly to the level instance)
            if (layerInstance.getEntityInstances() != null && layerInstance.getEntityInstances().length > 0) {
                processLevelInstances(entityList, List.of(layerInstance.getEntityInstances()), level); // process entities
            }
        }

        if (!layerList.isEmpty()) {
            builder.layers(layerList);
        }

        if (!entityList.isEmpty()) {
            builder.entities(entityList);
        }
    }

    /**
     * Processes the tile instances of the level.
     *
     * @param entityList         The list to add the entities to.
     * @param entityInstanceList The list of entity instances to process.
     * @param level              The level to process.
     */
    private void processLevelInstances(List<LdtkGameEntity> entityList, List<EntityInstance> entityInstanceList,
                                       Level level) {
        for (EntityInstance entityInstance : entityInstanceList) {
            entityList.add(
                    LdtkGameEntity.builder()
                            .identifier(entityInstance.getIdentifier())
                            .position(new Vector2(level.getWorldX() + entityInstance.getPx()[0],
                                    level.getWorldY() + entityInstance.getPx()[1]))
                            .pivot(new Vector2((float) entityInstance.getPivot()[0],
                                    (float) entityInstance.getPivot()[1]))
                            .width((int) entityInstance.getWidth())
                            .height((int) entityInstance.getHeight())
                            .build()
            );
        }
    }

    /**
     * Process a single tile instance.
     *
     * @param tileInstance  The tile instance to process.
     * @param layerInstance The layer instance the tile instance belongs to.
     * @param level         The level the tile instance belongs to.
     * @return The processed tile instance.
     */
    private LdtkGameLayerTile processTileInstance(TileInstance tileInstance, LayerInstance layerInstance,
                                                  Level level) {
        return LdtkGameLayerTile.builder()
                .tilesetSource(processTileSourceArea(tileInstance, layerInstance))
                .displayArea(
                        new Rectangle(level.getWorldX() + tileInstance.getPx()[0],
                                level.getWorldY() + tileInstance.getPx()[1],
                                layerInstance.getGridSize(),
                                layerInstance.getGridSize()))
                .position(
                        new Vector2(level.getWorldX() + tileInstance.getPx()[0],
                                level.getWorldY() + tileInstance.getPx()[1]))
                .build();
    }

    /**
     * Process the source area of a tile instance.
     *
     * @param tileInstance  The tile instance to process.
     * @param layerInstance The layer instance the tile instance belongs to.
     * @return The processed source area.
     */
    private Rectangle processTileSourceArea(TileInstance tileInstance, LayerInstance layerInstance) {
        float sx = tileInstance.getSrc()[0];
        float sy = tileInstance.getSrc()[1];
        float gridSize = layerInstance.getGridSize();

        switch ((int) tileInstance.getF()) {
            case 1: // flip horizontally
                return new Rectangle(sx + (sx + gridSize - sx), sy, -gridSize, gridSize);
            case 2: // flip vertically
                return new Rectangle(sx, sy + (sy + gridSize - sy), gridSize, -gridSize);
            case 3: // flip both
                return new Rectangle(sx + (sx + gridSize - sx), sy + (sy + gridSize - sy), -gridSize, -gridSize);
            default: // no flip
                return new Rectangle(sx, sy, gridSize, gridSize);
        }
    }
}
