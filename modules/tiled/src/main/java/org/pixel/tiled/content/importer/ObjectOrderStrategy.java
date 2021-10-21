package org.pixel.tiled.content.importer;

import org.pixel.commons.Pair;
import org.pixel.tiled.content.TiledObject;

import java.util.LinkedHashMap;
import java.util.List;

public abstract class ObjectOrderStrategy {
    public abstract LinkedHashMap<Integer, TiledObject> getMap(List<Pair<Integer, TiledObject>> list);
}
