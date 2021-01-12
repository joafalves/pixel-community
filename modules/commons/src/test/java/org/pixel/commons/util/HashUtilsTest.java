/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.commons.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author João Filipe Alves
 */
public class HashUtilsTest {

    @Test
    public void uidTest() {
        Assert.assertNotNull(HashUtils.generateUID());
        Assert.assertNotNull(HashUtils.generateUID());
    }
}
