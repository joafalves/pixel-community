/*
 * This software is available under Apache License
 * Copyright (c)
 */

package org.pixel.pipeline;

public interface DataPipelineFactory<T> {

    /**
     * DataPipeline factory method.
     *
     * @return An instance of DataPipeline
     */
    DataPipeline<T> create();
}
