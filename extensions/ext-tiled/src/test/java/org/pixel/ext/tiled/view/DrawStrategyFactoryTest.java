package org.pixel.ext.tiled.view;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pixel.ext.tiled.content.TiledMap;

class DrawStrategyFactoryTest {
    @Test
    public void createStrategy() {
        TiledDrawStrategyFactory drawStrategyFactory = new TiledDrawStrategyFactory();
        TiledMap tileMap = Mockito.mock(TiledMap.class);

        Mockito.when(tileMap.getRenderOrder()).thenReturn("right-down");
        Assertions.assertSame(TiledRightDownStrategy.class, drawStrategyFactory.getDrawStrategy(tileMap).getClass());

        Mockito.when(tileMap.getRenderOrder()).thenReturn("right-up");
        Assertions.assertSame(TiledRightUpStrategy.class, drawStrategyFactory.getDrawStrategy(tileMap).getClass());

        Mockito.when(tileMap.getRenderOrder()).thenReturn("left-down");
        Assertions.assertSame(TiledLeftDownStrategy.class, drawStrategyFactory.getDrawStrategy(tileMap).getClass());

        Mockito.when(tileMap.getRenderOrder()).thenReturn("left-up");
        Assertions.assertSame(TiledLeftUpStrategy.class, drawStrategyFactory.getDrawStrategy(tileMap).getClass());

        Mockito.when(tileMap.getRenderOrder()).thenReturn("");
        Assertions.assertSame(TiledRightDownStrategy.class, drawStrategyFactory.getDrawStrategy(tileMap).getClass());
    }
}