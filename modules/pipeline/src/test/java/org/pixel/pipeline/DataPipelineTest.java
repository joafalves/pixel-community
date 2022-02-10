/*
 * This software is available under Apache License
 * Copyright (c)
 */

package org.pixel.pipeline;

import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;

public class DataPipelineTest {

    @Test
    public void factoryTest() {
        DataPipelineFactory<String> factory = () -> new DataPipeline<>(new TestHandler());

        var dataPipelineA = factory.create();
        var dataPipelineB = factory.create();

        Assertions.assertNotNull(dataPipelineA);
        Assertions.assertNotNull(dataPipelineB);
        Assertions.assertNotEquals(dataPipelineA, dataPipelineB);
    }

    @Test
    public void dataTest() throws ExecutionException, InterruptedException {
        var dataPipeline = new DataPipeline<>(new TestHandler());
        var future = dataPipeline.begin("Dummy");

        Assertions.assertEquals(DataPipelineTest.class.getSimpleName(), future.get());
    }

    @Test
    public void threadPoolDataTest() throws ExecutionException, InterruptedException {
        var dataPipeline = new ThreadPoolDataPipeline<>(10, 20, "test", new TestHandler());
        var future = dataPipeline.begin("Dummy");

        Assertions.assertEquals(DataPipelineTest.class.getSimpleName(), future.get());
    }

    @Test
    public void multipleFutures() throws ExecutionException, InterruptedException {
        var dataPipeline = new ThreadPoolDataPipeline<>(10, 20, "test", new TestHandler());
        var futureA = dataPipeline.begin("DummyA");
        var futureB = dataPipeline.begin("DummyB");

        Assertions.assertEquals(DataPipelineTest.class.getSimpleName(), futureA.get());
        Assertions.assertEquals(DataPipelineTest.class.getSimpleName(), futureB.get());
    }

    private static class TestHandler implements PipelineHandler<String> {

        private static final Logger log = LoggerFactory.getLogger(TestHandler.class);

        @Override
        public void process(PipelineContext<String> ctx, String data) {
            log.debug("Received data: '{}'.", data);
            new Thread(() -> ctx.next(DataPipelineTest.class.getSimpleName())).start();
        }
    }
}
