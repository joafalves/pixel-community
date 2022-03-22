/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.commons.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.nio.ByteBuffer;

@Getter
@Builder
@AllArgsConstructor
public class ImageData {
    private final ByteBuffer data;
    private final int width;
    private final int height;
}
