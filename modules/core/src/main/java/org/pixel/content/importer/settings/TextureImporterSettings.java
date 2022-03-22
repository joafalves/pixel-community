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

    /**
     * Constructor.
     *
     * @param wrapSMode     Horizontal wrap mode.
     * @param wrapTMode     Vertical wrap mode.
     * @param minFilterMode Min filter mode.
     * @param magFilterMode Mag filter mode.
     */
    public TextureImporterSettings(int wrapSMode, int wrapTMode, int minFilterMode, int magFilterMode) {
        this.wrapSMode = wrapSMode;
        this.wrapTMode = wrapTMode;
        this.minFilterMode = minFilterMode;
        this.magFilterMode = magFilterMode;
    }

    /**
     * Getter for wrapSMode.
     *
     * @return The horizontal wrap mode.
     */
    public int getWrapSMode() {
        return wrapSMode;
    }

    /**
     * Setter for wrapSMode.
     *
     * @param wrapSMode The horizontal wrap mode.
     */
    public void setWrapSMode(int wrapSMode) {
        this.wrapSMode = wrapSMode;
    }

    /**
     * Getter for wrapTMode.
     *
     * @return The vertical wrap mode.
     */
    public int getWrapTMode() {
        return wrapTMode;
    }

    /**
     * Setter for wrapTMode.
     *
     * @param wrapTMode The vertical wrap mode.
     */
    public void setWrapTMode(int wrapTMode) {
        this.wrapTMode = wrapTMode;
    }

    /**
     * Getter for minFilterMode.
     *
     * @return The min filter mode.
     */
    public int getMinFilterMode() {
        return minFilterMode;
    }

    /**
     * Setter for minFilterMode.
     *
     * @param minFilterMode The min filter mode.
     */
    public void setMinFilterMode(int minFilterMode) {
        this.minFilterMode = minFilterMode;
    }

    /**
     * Getter for magFilterMode.
     *
     * @return The mag filter mode.
     */
    public int getMagFilterMode() {
        return magFilterMode;
    }

    /**
     * Setter for magFilterMode.
     *
     * @param magFilterMode The mag filter mode.
     */
    public void setMagFilterMode(int magFilterMode) {
        this.magFilterMode = magFilterMode;
    }
}
