/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.content.importer.settings;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TextureImporterSettings implements ContentImporterSettings {
    private int wrapSMode;
    private int wrapTMode;
    private int minFilterMode;
    private int magFilterMode;
    /**
     * Store colors premultiplied by alpha (the default): smooth filtering then has no fringes. Turn it off for data
     * textures whose channels aren't colors (e.g. distance-field font atlases).
     */
    @Builder.Default
    private boolean premultiplyAlpha = true;

    @Override
    public TextureImporterSettings copy() {
        return toBuilder().build();
    }
}
