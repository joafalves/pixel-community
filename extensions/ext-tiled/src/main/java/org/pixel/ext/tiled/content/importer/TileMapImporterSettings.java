package org.pixel.ext.tiled.content.importer;

import org.lwjgl.opengl.GL12;
import org.pixel.content.importer.settings.ContentImporterSettings;
import org.pixel.content.importer.settings.TextureImporterSettings;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_NEAREST;

public class TileMapImporterSettings implements ContentImporterSettings {
    private final TextureImporterSettings textureImporterSettings;
    private final List<TileMapProcessor> processors;

    public TileMapImporterSettings() {
        this(new TextureImporterSettings(GL12.GL_CLAMP_TO_EDGE, GL12.GL_CLAMP_TO_EDGE, GL_NEAREST, GL_NEAREST));
    }

    public TileMapImporterSettings(List<TileMapProcessor> processors) {
        this(new TextureImporterSettings(GL12.GL_CLAMP_TO_EDGE, GL12.GL_CLAMP_TO_EDGE, GL_NEAREST, GL_NEAREST), processors);
    }

    public TileMapImporterSettings(TextureImporterSettings settings) {
        this(settings, new ArrayList<>());

        processors.add(new TileSetProcessor());
        processors.add(new LayerProcessor());
    }

    public TileMapImporterSettings(TextureImporterSettings settings, List<TileMapProcessor> processors) {
        this.textureImporterSettings = settings;
        this.processors = processors;
    }

    public List<TileMapProcessor> getProcessors() {
        return processors;
    }

    public TextureImporterSettings getTextureImporterSettings() {
        return textureImporterSettings;
    }
}
