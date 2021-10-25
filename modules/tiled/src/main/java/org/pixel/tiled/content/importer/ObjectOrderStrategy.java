package org.pixel.tiled.content.importer;

import org.pixel.commons.Pair;
import org.pixel.tiled.content.DrawableTiledObject;

import java.util.LinkedHashMap;
import java.util.List;

public abstract class ObjectOrderStrategy {
    public abstract LinkedHashMap<Integer, DrawableTiledObject> getMap(List<Pair<Integer, DrawableTiledObject>> list);
}
