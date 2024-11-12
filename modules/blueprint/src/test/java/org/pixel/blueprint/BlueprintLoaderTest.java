package org.pixel.blueprint;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.pixel.blueprint.annotation.Auto;
import org.pixel.blueprint.annotation.Blueprint;
import org.pixel.blueprint.annotation.Component;
import org.pixel.blueprint.annotation.AfterAssembly;

class BlueprintLoaderTest {

    private static final String STR_A = "I'm a string!";
    private static final String STR_B = "I'm another string!";
    private static final int INITIAL_COUNT = 10;

    @BeforeAll
    public static void setup() {
        BlueprintLoader.load(new String[]{"org.pixel.blueprint"});
    }

    @Test
    void load() {
        // Example with post-assembly:
        var postAssemblyInst = new TestClass();
        BlueprintAssembler.assemble(postAssemblyInst);

        Assertions.assertEquals(STR_A, postAssemblyInst.someString);
        Assertions.assertEquals(STR_B, postAssemblyInst.anotherString);
        Assertions.assertEquals(STR_B, postAssemblyInst.specificString);
        Assertions.assertEquals(STR_A, postAssemblyInst.proxyString);
        Assertions.assertEquals(postAssemblyInst.specificString, postAssemblyInst.postAssemblyString);

        // Validate strict order-dependent values
        Assertions.assertEquals(INITIAL_COUNT, postAssemblyInst.baseValue);
        Assertions.assertEquals(INITIAL_COUNT + 5, postAssemblyInst.counter); // Should be 15
        Assertions.assertEquals((INITIAL_COUNT + 5) * 2, postAssemblyInst.dependentCounter); // Should be 30

        // Example with instance-assembly:
        var constructorAssemblyInst = BlueprintAssembler.assemble(TestClass.class);
        Assertions.assertEquals(STR_B, constructorAssemblyInst.constructorString);

        // Validate strict order-dependent values
        Assertions.assertEquals(INITIAL_COUNT, constructorAssemblyInst.baseValue);
        Assertions.assertEquals(INITIAL_COUNT + 5, constructorAssemblyInst.counter); // Should be 15
        Assertions.assertEquals((INITIAL_COUNT + 5) * 2, constructorAssemblyInst.dependentCounter); // Should be 30
    }

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
        public String proxyString(@Auto String someString) {
            return someString;
        }

        // New components to enforce strict dependency order
        @Component
        public Integer baseValue() {
            return INITIAL_COUNT;
        }

        @Component
        public Integer dependentCounter(@Auto Integer counter) {
            return counter * 2; // should be 30 if order is correct
        }

        @Component
        public Integer counter(@Auto Integer baseValue) {
            return baseValue + 5; // should be 15
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
        @Auto
        private Integer baseValue;
        @Auto
        private Integer counter;
        @Auto
        private Integer dependentCounter;

        private String postAssemblyString;

        private final String constructorString;

        public TestClass(@Auto("specificString") String constructorString) {
            this.constructorString = constructorString;
        }

        public TestClass() {
            this.constructorString = STR_B;
        }

        @AfterAssembly
        public void postAssembly(@Auto String specificString) {
            postAssemblyString = specificString;
        }
    }
}
