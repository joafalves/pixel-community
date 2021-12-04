/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.input.gamepad;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class GamePadState {

    private final String name;
    private final ByteBuffer buttonState;
    private final FloatBuffer axeState;

    /**
     * Constructor
     *
     * @param name
     */
    public GamePadState(String name, ByteBuffer buttonState, FloatBuffer axeState) {
        this.name = name;
        this.buttonState = buttonState;
        this.axeState = axeState;
    }

    /**
     * @param button
     * @return
     */
    public boolean isButtonDown(GamePadButton button) {
        return buttonState.get(button.getValue()) == 1;
    }

    /**
     * @param button
     * @return
     */
    public boolean isButtonUp(GamePadButton button) {
        return buttonState.get(button.getValue()) == 0;
    }

    /**
     * @param axe
     * @return
     */
    public float getAxeValue(GamePadAxe axe) {
        return axeState.get(axe.getValue());
    }

    /**
     * @return
     */
    public String getName() {
        return name;
    }
}
