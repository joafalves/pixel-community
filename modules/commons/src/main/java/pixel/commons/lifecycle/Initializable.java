/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.commons.lifecycle;

public interface Initializable {
    /**
     * Initialize function
     *
     * @return true when initialized with success
     */
    boolean init();
}
