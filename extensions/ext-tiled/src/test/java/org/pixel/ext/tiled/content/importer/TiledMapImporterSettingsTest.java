package org.pixel.ext.tiled.content.importer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

class TiledMapImporterSettingsTest {
    @Test
    void constructor() {
        List<TileMapProcessor> processorList = new ArrayList<>();
        processorList.add(Mockito.mock(TileMapProcessor.class));
        TiledMapImporterSettings settings = new TiledMapImporterSettings(processorList);

        Assertions.assertEquals(GL12.GL_CLAMP_TO_EDGE, settings.getTextureImporterSettings().getWrapSMode());
        Assertions.assertEquals(GL12.GL_CLAMP_TO_EDGE, settings.getTextureImporterSettings().getWrapTMode());
        Assertions.assertEquals(GL11.GL_NEAREST, settings.getTextureImporterSettings().getMagFilterMode());
        Assertions.assertEquals(GL11.GL_NEAREST, settings.getTextureImporterSettings().getMinFilterMode());

        Assertions.assertSame(processorList, settings.getProcessors());
    }
}