package org.pixel.tiled.view;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pixel.tiled.content.TileMap;

import static org.junit.jupiter.api.Assertions.*;

class DrawStrategyFactoryTest {
    @Test
    public void createStrategy() {
        DrawStrategyFactory drawStrategyFactory = new DrawStrategyFactory();
        TileMap tileMap = Mockito.mock(TileMap.class);

        Mockito.when(tileMap.getRenderOrder()).thenReturn("right-down");
        Assertions.assertSame(RightDownStrategy.class, drawStrategyFactory.getDrawStrategy(tileMap).getClass());

        Mockito.when(tileMap.getRenderOrder()).thenReturn("right-up");
        Assertions.assertSame(RightUpStrategy.class, drawStrategyFactory.getDrawStrategy(tileMap).getClass());

        Mockito.when(tileMap.getRenderOrder()).thenReturn("left-down");
        Assertions.assertSame(LeftDownStrategy.class, drawStrategyFactory.getDrawStrategy(tileMap).getClass());

        Mockito.when(tileMap.getRenderOrder()).thenReturn("left-up");
        Assertions.assertSame(LeftUpStrategy.class, drawStrategyFactory.getDrawStrategy(tileMap).getClass());

        Mockito.when(tileMap.getRenderOrder()).thenReturn("");
        Assertions.assertSame(RightDownStrategy.class, drawStrategyFactory.getDrawStrategy(tileMap).getClass());
    }
}