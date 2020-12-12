/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.physics;

import org.junit.Assert;
import org.junit.Test;
import pixel.math.Vector2;
import pixel.physics.shape.Circle;

import java.util.ArrayList;
import java.util.List;

public class CollisionTest {

    @Test
    public void circleCollision() {
        Body a = new Body(new Vector2());
        a.setShape(new Circle(10.f));
        Body b = new Body(new Vector2(8));
        b.setShape(new Circle(10.f));

        ArrayList<Body> bodyList = new ArrayList<>();
        bodyList.add(a);
        bodyList.add(b);

        CollisionManager manager = new CollisionManager();
        List<CollisionGroup> collisions = manager.detectCollisions(bodyList);

        Assert.assertTrue(collisions.size() > 0);
    }

}
