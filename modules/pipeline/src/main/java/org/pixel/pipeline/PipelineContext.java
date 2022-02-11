/*
 * This software is available under Apache License
 * Copyright (c)
 */

package org.pixel.pipeline;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;

public class PipelineContext<T> {

    private static final Logger log = LoggerFactory.getLogger(PipelineContext.class);

    private final DataPipeline<T> dataPipeline;
    private final CompletableFuture<T> future;
    private Iterator<PipelineHandler<T>> iterator;

    /**
     * Constructor.
     *
     * @param dataPipeline The data pipeline associated with this context.
     */
    public PipelineContext(DataPipeline<T> dataPipeline, CompletableFuture<T> future) {
        this.dataPipeline = dataPipeline;
        this.future = future;
        this.iterator = null;
    }

    /**
     * Resolves current handler processing and pushes the output to the next handler (or the end of the pipeline).
     *
     * @param output The output of the current handler.
     */
    public void next(T output) {
        if (iterator == null) {
            log.trace("Getting new handler iterator due to being null.");
            iterator = dataPipeline.getHandlerIterator();
        }

        if (!iterator.hasNext()) {
            // Pipeline has reached the end.
            log.trace("Pipeline has reached the end, notifying listeners...");

            iterator = null;
            future.complete(output);

            return;
        }

        iterator.next().process(this, output);
    }

    /**
     * Resolves the current handler processing and terminates the pipeline processing with the given output.
     *
     * @param output The output of the pipeline.
     */
    public void resolve(T output) {
        iterator = null;
        future.complete(output);
    }
}
