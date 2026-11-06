package org.pixel.ext.decs;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WorldTest {

    static class Shape implements Component {
    }

    static class CircleShape extends Shape {
    }

    static class SquareShape extends Shape {
    }

    static class Health implements Component {
    }

    @Test
    void getComponentFindsSubclassesLikeHasComponentAndGroups() {
        World world = new World();
        Entity e = world.createEntity();
        CircleShape circle = new CircleShape();
        world.addComponent(e, circle);

        assertTrue(world.hasComponent(e, Shape.class));
        assertSame(circle, world.getComponent(e, Shape.class));
        assertSame(circle, world.getComponent(e, CircleShape.class));
        assertTrue(world.getGroup(Shape.class).getEntities().contains(e));
    }

    @Test
    void removeComponentBySuperTypeUpdatesGroups() {
        World world = new World();
        Entity e = world.createEntity();
        world.addComponent(e, new CircleShape());
        Group shapes = world.getGroup(Shape.class);

        world.removeComponent(e, Shape.class);

        assertFalse(world.hasComponent(e, Shape.class));
        assertFalse(shapes.getEntities().contains(e));
    }

    @Test
    void entityStaysInGroupWhileAnotherComponentStillMatches() {
        World world = new World();
        Entity e = world.createEntity();
        world.addComponent(e, new CircleShape());
        world.addComponent(e, new SquareShape());
        Group shapes = world.getGroup(Shape.class);

        world.removeComponent(e, CircleShape.class);

        assertTrue(shapes.getEntities().contains(e));
    }

    @Test
    void entitiesCanBeDestroyedAndCreatedWhileIteratingAGroup() {
        World world = new World();
        for (int i = 0; i < 5; i++) {
            world.addComponent(world.createEntity(), new Health());
        }
        Group group = world.getGroup(Health.class);

        List<Entity> visited = new ArrayList<>();
        assertDoesNotThrow(() -> {
            for (Entity entity : group) {
                visited.add(entity);
                world.destroyEntity(entity);
                world.addComponent(world.createEntity(), new Health());
            }
        });

        assertEquals(5, visited.size()); // new entities are not visited in the same pass
        assertEquals(5, group.getEntities().size());
    }

    @Test
    void entitiesRemovedDuringIterationAreSkipped() {
        World world = new World();
        Entity a = world.createEntity();
        Entity b = world.createEntity();
        world.addComponent(a, new Health());
        world.addComponent(b, new Health());

        List<Entity> visited = new ArrayList<>();
        for (Entity entity : world.getGroup(Health.class)) {
            visited.add(entity);
            world.destroyEntity(entity.equals(a) ? b : a); // groups hold their own Entity instances
        }

        assertEquals(1, visited.size());
    }
}
