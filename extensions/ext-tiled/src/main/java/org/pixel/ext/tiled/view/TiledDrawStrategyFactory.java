package org.pixel.ext.tiled.view;

import org.pixel.ext.tiled.content.TiledMap;

import java.util.HashMap;

public class TiledDrawStrategyFactory {
    private final static HashMap<String, TiledDrawStrategy> renderOrderToStrategy = new HashMap<>();

    static {
        renderOrderToStrategy.put("right-down", new TiledRightDownStrategy());
        renderOrderToStrategy.put("right-up", new TiledRightUpStrategy());
        renderOrderToStrategy.put("left-down", new TiledLeftDownStrategy());
        renderOrderToStrategy.put("left-up", new TiledLeftUpStrategy());
    }

    public TiledDrawStrategy getDrawStrategy(TiledMap tileMap) {
        return renderOrderToStrategy.getOrDefault(tileMap.getRenderOrder(), new TiledRightDownStrategy());
    }
}
