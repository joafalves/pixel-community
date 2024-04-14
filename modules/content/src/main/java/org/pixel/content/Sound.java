/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.content;

import lombok.Getter;
import lombok.Setter;
import org.pixel.commons.lifecycle.Disposable;
import org.pixel.math.Vector2;

@Getter
@Setter
public abstract class Sound implements Disposable {
    private float gain = 1.0f;
    private float offset = 0.0f;
    private float pitch = 1.0f;
    private Vector2 spatialPosition = Vector2.zero();
}
