/*
 * This software is available under Apache License
 * Copyright (c)
 */

package org.pixel.pipeline;

public interface PipelineHandler<T> {

    /**
     * Handle and process the given data.
     *
     * @param ctx  The pipeline context.
     * @param data The data to process.
     */
    void process(PipelineContext<T> ctx, T data);
}
