/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.commons.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HashUtilsTest {

    @Test
    public void uniqueIdGenerationTest() {
        Assertions.assertNotNull(HashUtils.generateUID());
    }
}
