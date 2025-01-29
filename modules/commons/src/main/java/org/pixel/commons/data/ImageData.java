/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.commons.data;

import lombok.Builder;

import java.nio.ByteBuffer;

@Builder
public record ImageData(ByteBuffer data, int width, int height) {
}
