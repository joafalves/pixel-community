/*
 * This software is available under Apache License
 * Copyright (c)
 */

package org.pixel.pipeline;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.pixel.commons.lifecycle.Disposable;

public class ThreadPoolDataPipeline<T> extends DataPipeline<T> implements Disposable {

    private ThreadPoolExecutor threadPoolExecutor;
    private boolean disposed = false;

    /**
     * Constructor.
     *
     * @param threadPoolCoreSize The core size of the thread pool.
     * @param threadPoolMaxSize  The max size of the thread pool.
     */
    public ThreadPoolDataPipeline(int threadPoolCoreSize, int threadPoolMaxSize) {
        super();
        this.initThreadPool(threadPoolCoreSize, threadPoolMaxSize, ThreadPoolDataPipeline.class.getSimpleName());
    }

    /**
     * Constructor.
     *
     * @param threadPoolCoreSize The core size of the thread pool.
     * @param threadPoolMaxSize  The max size of the thread pool.
     * @param threadPrefix       The prefix of the thread name.
     */
    public ThreadPoolDataPipeline(int threadPoolCoreSize, int threadPoolMaxSize, String threadPrefix) {
        super();
        this.initThreadPool(threadPoolCoreSize, threadPoolMaxSize, threadPrefix);
    }

    /**
     * Constructor.
     *
     * @param threadPoolCoreSize The core size of the thread pool.
     * @param threadPoolMaxSize  The max size of the thread pool.
     * @param threadPrefix       The prefix of the thread name.
     * @param handlers           The handlers to be added to the pipeline.
     */
    @SafeVarargs
    public ThreadPoolDataPipeline(int threadPoolCoreSize, int threadPoolMaxSize, String threadPrefix,
            PipelineHandler<T>... handlers) {
        super(handlers);
        this.initThreadPool(threadPoolCoreSize, threadPoolMaxSize, threadPrefix);
    }

    @Override
    public CompletableFuture<T> begin(T data) {
        if (disposed) {
            throw new RuntimeException("Cannot process, pipeline has already been disposed.");
        }

        final var future = new CompletableFuture<T>();
        threadPoolExecutor.submit(() -> new PipelineContext<>(this, future).next(data));

        return future;
    }

    @Override
    public void dispose() {
        disposed = true;
        if (this.threadPoolExecutor != null) {
            this.threadPoolExecutor.shutdownNow();
            this.threadPoolExecutor.purge();
        }
    }

    private void initThreadPool(int threadPoolCoreSize, int threadPoolMaxSize, String threadPrefix) {
        this.threadPoolExecutor = new ThreadPoolExecutor(threadPoolCoreSize, threadPoolMaxSize, 1L,
                TimeUnit.SECONDS, new SynchronousQueue<>(), new NamedThreadFactory(threadPrefix),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    private static class NamedThreadFactory implements ThreadFactory {

        private long uid = 0L;
        private final String threadPrefix;

        private NamedThreadFactory(String threadPrefix) {
            this.threadPrefix = threadPrefix;
        }

        @Override
        public synchronized Thread newThread(Runnable runnable) {
            if (++uid < 0) {
                uid = 1L;
            }

            return new Thread(runnable, threadPrefix + "-" + uid);
        }
    }
}
