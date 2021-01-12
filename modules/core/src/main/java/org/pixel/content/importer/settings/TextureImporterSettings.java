/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.content.importer.settings;

public class TextureImporterSettings implements ContentImporterSettings {

    private int wrapSMode;
    private int wrapTMode;
    private int minFilterMode;
    private int magFilterMode;

    public TextureImporterSettings(int wrapSMode, int wrapTMode, int minFilterMode, int magFilterMode) {
        this.wrapSMode = wrapSMode;
        this.wrapTMode = wrapTMode;
        this.minFilterMode = minFilterMode;
        this.magFilterMode = magFilterMode;
    }

    public int getWrapSMode() {
        return wrapSMode;
    }

    public void setWrapSMode(int wrapSMode) {
        this.wrapSMode = wrapSMode;
    }

    public int getWrapTMode() {
        return wrapTMode;
    }

    public void setWrapTMode(int wrapTMode) {
        this.wrapTMode = wrapTMode;
    }

    public int getMinFilterMode() {
        return minFilterMode;
    }

    public void setMinFilterMode(int minFilterMode) {
        this.minFilterMode = minFilterMode;
    }

    public int getMagFilterMode() {
        return magFilterMode;
    }

    public void setMagFilterMode(int magFilterMode) {
        this.magFilterMode = magFilterMode;
    }
}
