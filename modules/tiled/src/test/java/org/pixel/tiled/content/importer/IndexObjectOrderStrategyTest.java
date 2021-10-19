package org.pixel.tiled.content.importer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pixel.commons.Pair;
import org.pixel.tiled.content.TiledObject;

import java.util.*;

class IndexObjectOrderStrategyTest {
    @Test
    public void getMap() {
        IndexObjectOrderStrategy strategy = new IndexObjectOrderStrategy();

        List<Pair<Integer, TiledObject>> objects = new ArrayList<>();

        objects.add(new Pair<>(1, Mockito.mock(TiledObject.class)));
        objects.add(new Pair<>(2, Mockito.mock(TiledObject.class)));
        objects.add(new Pair<>(3, Mockito.mock(TiledObject.class)));

        LinkedHashMap<Integer, TiledObject> map = strategy.getMap(objects);
        Iterator<Integer> keySetIt = map.keySet().iterator();
        Iterator<TiledObject> valuesIt = map.values().iterator();

        Assertions.assertTrue(map.containsKey(1));
        Assertions.assertTrue(map.containsKey(2));
        Assertions.assertTrue(map.containsKey(3));

        Assertions.assertEquals(1, keySetIt.next());
        Assertions.assertSame(objects.get(0).getB(), valuesIt.next());

        Assertions.assertEquals(2, keySetIt.next());
        Assertions.assertSame(objects.get(1).getB(), valuesIt.next());

        Assertions.assertEquals(3, keySetIt.next());
        Assertions.assertSame(objects.get(2).getB(), valuesIt.next());

        Assertions.assertFalse(keySetIt.hasNext());
        Assertions.assertFalse(valuesIt.hasNext());
    }

}