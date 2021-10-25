package org.pixel.tiled.content.importer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pixel.commons.Pair;
import org.pixel.math.Vector2;
import org.pixel.tiled.content.DrawableTiledObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

class TopDownObjectOrderStrategyTest {
    @Test
    public void getMap() {
        DrawableTiledObject object1 = Mockito.mock(DrawableTiledObject.class);
        DrawableTiledObject object2 = Mockito.mock(DrawableTiledObject.class);
        DrawableTiledObject object3 = Mockito.mock(DrawableTiledObject.class);
        Vector2 position1 = Mockito.mock(Vector2.class);
        Vector2 position2 = Mockito.mock(Vector2.class);
        Vector2 position3 = Mockito.mock(Vector2.class);

        Mockito.when(position1.getY()).thenReturn(2f);
        Mockito.when(position2.getY()).thenReturn(1f);
        Mockito.when(position3.getY()).thenReturn(3f);

        Mockito.when(object1.getPosition()).thenReturn(position1);
        Mockito.when(object2.getPosition()).thenReturn(position2);
        Mockito.when(object3.getPosition()).thenReturn(position3);

        TopDownObjectOrderStrategy strategy = new TopDownObjectOrderStrategy();

        List<Pair<Integer, DrawableTiledObject>> objects = new ArrayList<>();

        objects.add(new Pair<>(1, object1));
        objects.add(new Pair<>(2, object2));
        objects.add(new Pair<>(3, object3));

        LinkedHashMap<Integer, DrawableTiledObject> map = strategy.getMap(objects);
        Iterator<Integer> keySetIt = map.keySet().iterator();
        Iterator<DrawableTiledObject> valuesIt = map.values().iterator();

        Assertions.assertTrue(map.containsKey(1));
        Assertions.assertTrue(map.containsKey(2));
        Assertions.assertTrue(map.containsKey(3));

        Assertions.assertEquals(2, keySetIt.next());
        Assertions.assertSame(objects.get(1).getB(), valuesIt.next());

        Assertions.assertEquals(1, keySetIt.next());
        Assertions.assertSame(objects.get(0).getB(), valuesIt.next());

        Assertions.assertEquals(3, keySetIt.next());
        Assertions.assertSame(objects.get(2).getB(), valuesIt.next());

        Assertions.assertFalse(keySetIt.hasNext());
        Assertions.assertFalse(valuesIt.hasNext());

    }
}