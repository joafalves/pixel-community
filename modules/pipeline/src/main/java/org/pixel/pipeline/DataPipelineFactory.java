/*
 * This software is available under Apache License
 * Copyright (c)
 */

package org.pixel.pipeline;

public interface DataPipelineFactory<T> {
    DataPipeline<T> create();
}
