/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.input.gamepad;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class GamePadState implements Serializable {

    private final String name;
    private final ByteBuffer buttonState;
    private final FloatBuffer axeState;

    /**
     * Constructor.
     *
     * @param name        The name of the gamepad.
     * @param buttonState The button state.
     * @param axeState    The axe state.
     */
    public GamePadState(String name, ByteBuffer buttonState, FloatBuffer axeState) {
        this.name = name;
        this.buttonState = buttonState;
        this.axeState = axeState;
    }

    /**
     * Indicates if the given button is down.
     *
     * @param button The button to check.
     * @return True if the button is down, false otherwise.
     */
    public boolean isButtonDown(GamePadButton button) {
        return buttonState.get(button.getValue()) == 1;
    }

    /**
     * Indicates if the given button is up.
     *
     * @param button The button to check.
     * @return True if the button is up, false otherwise.
     */
    public boolean isButtonUp(GamePadButton button) {
        return buttonState.get(button.getValue()) == 0;
    }

    /**
     * Get the axe value of the given axe.
     *
     * @param axe The axe to get the value.
     * @return The axe value.
     */
    public float getAxeValue(GamePadAxe axe) {
        return axeState.get(axe.getValue());
    }

    /**
     * Get the name of the gamepad.
     *
     * @return The name of the gamepad.
     */
    public String getName() {
        return name;
    }
}
