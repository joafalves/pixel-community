package org.pixel.tiled.content.importer;

import org.pixel.commons.Pair;
import org.pixel.tiled.content.TiledObject;

import java.util.LinkedHashMap;
import java.util.List;

public class IndexObjectOrderStrategy extends ObjectOrderStrategy {
    @Override
    public LinkedHashMap<Integer, TiledObject> getMap(List<Pair<Integer, TiledObject>> list) {
        LinkedHashMap<Integer, TiledObject> objectMap = new LinkedHashMap<>();

        list.forEach(entry -> objectMap.put(entry.getA(), entry.getB()));

        return objectMap;
    }
}
