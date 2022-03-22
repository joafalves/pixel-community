/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.physics;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.pixel.math.Vector2;
import org.pixel.physics.shape.CircleShape;

public class CollisionTest {

    @Test
    public void circleCollision() {
        Body a = new Body(new Vector2());
        a.setShape(new CircleShape(10.f));
        Body b = new Body(new Vector2(8));
        b.setShape(new CircleShape(10.f));

        ArrayList<Body> bodyList = new ArrayList<>();
        bodyList.add(a);
        bodyList.add(b);

        CollisionManager manager = new CollisionManager();
        List<CollisionGroup> collisions = manager.detectCollisions(bodyList);

        Assertions.assertTrue(collisions.size() > 0);
    }

}
