package org.pixel.ext.ecs;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.pixel.content.Texture;

public class GameObjectTest {

    @Test
    public void getChildTest() {
        GameObject parent = new GameObject("Parent");
        GameObject child = new Sprite("Child", (Texture) null);
        parent.addChild(child);

        Assertions.assertNotNull(parent.getChild(Sprite.class));
        Assertions.assertNotNull(parent.getChild("Child"));
    }

    @Test
    public void getChildRecursiveTest() {
        GameObject parent = new GameObject("Parent");
        GameObject childL1 = new Sprite("Child", (Texture) null);
        GameObject childL2 = new Sprite("Child", (Texture) null);
        GameObject childL3 = new Sprite("Child_Other_Name", (Texture) null);

        parent.addChild(childL1);
        childL1.addChild(childL2);
        childL2.addChild(childL3);

        Assertions.assertEquals(3, parent.getAllChildren(Sprite.class).size());
        Assertions.assertEquals(2, parent.getAllChildren("Child").size());
    }

    @Test
    public void getComponentTest() {
        GameObject testObject = new GameObject("TestObject");
        testObject.addComponent(new DummyComponent());

        Assertions.assertNotNull(testObject.getComponent(DummyComponent.class));
        Assertions.assertNull(testObject.getComponent("something"));
    }

    @Test
    public void getComponentRecursiveTest() {
        GameObject parent = new GameObject("Parent");
        GameObject childL1_1 = new Sprite("Child", (Texture) null);
        GameObject childL1_2 = new Sprite("Child", (Texture) null);
        GameObject childL2_1 = new Sprite("Child", (Texture) null);

        childL1_1.addComponent(new DummyComponent());
        childL1_2.addComponent(new DummyComponent());
        childL2_1.addComponent(new DummyComponent());
        childL2_1.addComponent(new DummyComponent("OtherName"));

        parent.addChild(childL1_1);
        parent.addChild(childL1_2);
        childL1_1.addChild(childL2_1);

        Assertions.assertEquals(4, parent.getAllComponents(DummyComponent.class).size());
        Assertions.assertEquals(3, parent.getAllComponents("").size());
        Assertions.assertEquals(1, childL1_1.getAllComponents("OtherName").size());
    }

    private static class DummyComponent extends GameComponent {
        // intentionally empty
        public DummyComponent() {

        }

        public DummyComponent(String name) {
            super(name);
        }
    }
}
