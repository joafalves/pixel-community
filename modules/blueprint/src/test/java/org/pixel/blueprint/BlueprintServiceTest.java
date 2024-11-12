package org.pixel.blueprint;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.pixel.blueprint.annotation.Auto;
import org.pixel.blueprint.annotation.Blueprint;
import org.pixel.blueprint.annotation.Component;
import org.pixel.blueprint.annotation.Service;

import static org.junit.jupiter.api.Assertions.*;

class BlueprintServiceTest {

    @BeforeAll
    static void setup() {
        // Initialize the global repository and load configurations in the test package
        BlueprintLoader.load(new String[]{"org.pixel.blueprint"});
    }

    @Test
    void testServiceInstantiation() {
        // Retrieve the TestService from the repository
        TestService testService = BlueprintRepository.getDefault().ugetService(TestService.class, "testService");

        assertNotNull(testService, "TestService should be instantiated and registered.");
        assertEquals("Hello, World!", testService.greet(), "TestService should greet properly.");
    }

    @Test
    void testServiceDependencyInjection() {
        // Retrieve the TestService with injected dependency (using default uget for test purposes)
        TestService testService = BlueprintRepository.getDefault().uget(TestService.class, "testService");

        assertNotNull(testService, "TestService should be instantiated and registered.");
        assertNotNull(testService.getGreetingComponent(), "GreetingComponent should be injected into TestService.");
        assertEquals("Hello, World!", testService.greet(), "Injected GreetingComponent should provide correct greeting.");
    }

    @Blueprint
    static class TestConfiguration {

        @Component("greetingComponent")
        public GreetingComponent greetingComponent() {
            return new GreetingComponent("Hello, World!");
        }
    }

    @Service("testService")
    static class TestService {

        private final GreetingComponent greetingComponent;

        // Constructor injection
        public TestService(@Auto("greetingComponent") GreetingComponent greetingComponent) {
            this.greetingComponent = greetingComponent;
        }

        public String greet() {
            return greetingComponent.getMessage();
        }

        public GreetingComponent getGreetingComponent() {
            return greetingComponent;
        }
    }

    static class GreetingComponent {
        private final String message;

        public GreetingComponent(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
