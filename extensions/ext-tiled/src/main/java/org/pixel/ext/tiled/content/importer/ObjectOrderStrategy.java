package org.pixel.ext.tiled.content.importer;

import org.pixel.commons.data.Pair;
import org.pixel.ext.tiled.content.TiledObject;

import java.util.LinkedHashMap;
import java.util.List;

public abstract class ObjectOrderStrategy {
    public abstract LinkedHashMap<Integer, TiledObject> getMap(List<Pair<Integer, TiledObject>> list);
}
