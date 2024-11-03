package org.pixel.blueprint;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.pixel.blueprint.annotation.Auto;
import org.pixel.blueprint.annotation.Blueprint;
import org.pixel.blueprint.annotation.Component;

class BlueprintServiceTest {

    private static final String STR_A = "I'm a string!";
    private static final String STR_B = "I'm another string!";

    @Blueprint
    public static class BlueprintConfig {

        @Component
        public String someString() {
            return STR_A;
        }

        @Component("specificString")
        public String anotherString() {
            return STR_B;
        }

        @Component
        public String proxyString(String someString) {
            return someString;
        }
    }

    public static class TestClass {
        @Auto
        private String someString;
        @Auto("specificString")
        private String anotherString;
        @Auto
        private String specificString;
        @Auto
        private String proxyString;

        private final String constructorString;

        public TestClass(@Auto("specificString") String constructorString) {
            this.constructorString = constructorString;
        }

        public TestClass() {
            this.constructorString = STR_B;
            ComponentAssembler.assemble(this);
        }
    }

    @Test
    void load() {
        BlueprintLoader.load(new String[]{"org.pixel"});

        // Using AUTO proxy (automatic assignment):
        var testClass = new TestClass();

        Assertions.assertEquals(STR_A, testClass.someString);
        Assertions.assertEquals(STR_B, testClass.anotherString);
        Assertions.assertEquals(STR_B, testClass.specificString);
        Assertions.assertEquals(STR_A, testClass.proxyString);

        // Alternative (if the class didn't have Auto):
        var anotherTestClass = ComponentAssembler.assemble(TestClass.class);
        Assertions.assertEquals(STR_B, anotherTestClass.constructorString);
    }
}