/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.content;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.pixel.commons.lifecycle.Disposable;

@Getter
@Setter
@AllArgsConstructor
public abstract class Texture implements Disposable {
    private int width;
    private int height;
}
