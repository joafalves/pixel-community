/*
 * This software is available under Apache License
 * Copyright (c)
 */

package org.pixel.pipeline;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;

public class DataPipeline<T> {

    private final LinkedList<PipelineHandler<T>> handlerList;

    /**
     * Constructor.
     */
    public DataPipeline() {
        this.handlerList = new LinkedList<>();
    }

    /**
     * Constructor.
     *
     * @param handlers The handlers to add to the pipeline.
     */
    @SafeVarargs
    public DataPipeline(PipelineHandler<T>... handlers) {
        this.handlerList = new LinkedList<>();
        Collections.addAll(this.handlerList, handlers);
    }

    /**
     * Begin processing the data.
     *
     * @param data The data to process.
     */
    public CompletableFuture<T> begin(T data) {
        var future = new CompletableFuture<T>();
        new PipelineContext<>(this, future).next(data);
        return future;
    }

    /**
     * Get the handler list iterator.
     *
     * @return The handler list iterator.
     */
    public Iterator<PipelineHandler<T>> getHandlerIterator() {
        return this.handlerList.iterator();
    }
}
