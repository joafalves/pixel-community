package org.pixel.core.dev;

/**
 * When the dev tools overlay is available ({@code GameSettings.setDevToolsMode}).
 */
public enum DevToolsMode {

    /** Available when the game runs in dev mode ({@code GameSettings.setDevMode(true)}). The default. */
    DEV_MODE,
    /** Always available (e.g. a test build). */
    ON,
    /** Never available: no overlay, no log capture, no cost. */
    OFF
}
