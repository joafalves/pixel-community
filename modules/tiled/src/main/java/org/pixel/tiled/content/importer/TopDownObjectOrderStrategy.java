package org.pixel.tiled.content.importer;

import org.pixel.commons.Pair;
import org.pixel.tiled.content.DrawableTiledObject;

import java.util.LinkedHashMap;
import java.util.List;

public class TopDownObjectOrderStrategy extends ObjectOrderStrategy {
    @Override
    public LinkedHashMap<Integer, DrawableTiledObject> getMap(List<Pair<Integer, DrawableTiledObject>> list) {
        LinkedHashMap<Integer, DrawableTiledObject> objectMap = new LinkedHashMap<>();
        list.stream()
                .sorted((a, b) -> Float.compare(a.getB().getPosition().getY(), b.getB().getPosition().getY()))
                .forEach(entry -> objectMap.put(entry.getA(), entry.getB()));

        return objectMap;
    }
}
