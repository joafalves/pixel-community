package org.pixel.ext.tiled.content.importer;

import org.pixel.commons.data.Pair;
import org.pixel.ext.tiled.content.TiledObject;

import java.util.LinkedHashMap;
import java.util.List;

class TopDownObjectOrderStrategy extends ObjectOrderStrategy {
    @Override
    public LinkedHashMap<Integer, TiledObject> getMap(List<Pair<Integer, TiledObject>> list) {
        LinkedHashMap<Integer, TiledObject> objectMap = new LinkedHashMap<>();
        list.stream()
                .sorted((a, b) -> Float.compare(a.getB().getPosition().getY(), b.getB().getPosition().getY()))
                .forEach(entry -> objectMap.put(entry.getA(), entry.getB()));

        return objectMap;
    }
}
