/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.content.importer.settings;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TextureImporterSettings implements ContentImporterSettings {
    private int wrapSMode;
    private int wrapTMode;
    private int minFilterMode;
    private int magFilterMode;
}
