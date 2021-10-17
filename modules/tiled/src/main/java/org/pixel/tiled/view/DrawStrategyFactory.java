package org.pixel.tiled.view;

import org.pixel.tiled.content.TileMap;

import java.util.HashMap;

public class DrawStrategyFactory {
    private final static HashMap<String, DrawStrategy> renderOrderToStrategy = new HashMap<>();
    static {
        renderOrderToStrategy.put("right-down", new RightDownStrategy());
        renderOrderToStrategy.put("right-up", new RightUpStrategy());
        renderOrderToStrategy.put("left-down", new LeftDownStrategy());
        renderOrderToStrategy.put("left-up", new LeftUpStrategy());
    }

    public DrawStrategy getDrawStrategy(TileMap tileMap) {
        return renderOrderToStrategy.getOrDefault(tileMap.getRenderOrder(), new RightDownStrategy());
    }
}
