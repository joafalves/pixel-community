/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.audio;

import org.junit.Assert;
import org.junit.Test;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;

import static org.lwjgl.openal.ALC10.*;

/**
 * @author João Filipe Alves
 */
public class AudioTest {

    @Test
    public void deviceTest() {
        String defaultDeviceName = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
        Assert.assertNotNull(defaultDeviceName);

        long device = alcOpenDevice(defaultDeviceName);

        // associate context...
        int[] attributes = {0};
        long context = alcCreateContext(device, attributes);
        alcMakeContextCurrent(context);

        ALCCapabilities alcCapabilities = ALC.createCapabilities(device);
        ALCapabilities alCapabilities = AL.createCapabilities(alcCapabilities);

        Assert.assertNotNull(alcCapabilities);
        Assert.assertNotNull(alCapabilities);
    }
}
