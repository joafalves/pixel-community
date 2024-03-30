package org.pixel.graphics.shader;

import org.pixel.commons.lifecycle.Disposable;
import org.pixel.commons.lifecycle.Initializable;

public interface Shader extends Initializable, Disposable {

    /**
     * Apply the shader properties.
     */
    void apply();

    /**
     * Set this shader instance as the active shader.
     */
    void use();

}
