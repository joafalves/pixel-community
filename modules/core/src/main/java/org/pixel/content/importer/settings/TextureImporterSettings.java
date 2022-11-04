/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.content.importer.settings;

import static org.lwjgl.opengl.GL11C.GL_NEAREST;
import static org.lwjgl.opengl.GL11C.GL_REPEAT;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TextureImporterSettings implements ContentImporterSettings {
    private int wrapSMode = GL_REPEAT;
    private int wrapTMode = GL_REPEAT;
    private int minFilterMode = GL_NEAREST;
    private int magFilterMode = GL_NEAREST;
}
