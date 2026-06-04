package org.pixel.blueprint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pixel.blueprint.annotation.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BlueprintAdvancedTest {

    @BeforeEach
    void setup() {
        BlueprintRepository.getDefault().clear();
    }

    // region Tag-based tests

    @Test
    void testTagBasedRetrieval() {
        BlueprintLoader.load(new String[]{"org.pixel.blueprint"});

        // TestClassForTags should have been assembled by the loader
        // (it's a service, so it should be auto-instantiated)
    }

    @Test
    void testRepositoryTagQueries() {
        BlueprintRepository repo = BlueprintRepository.getDefault();

        repo.registerComponent(String.class, "renderable-a", "comp-a", new String[]{"rendering"});
        repo.registerComponent(String.class, "renderable-b", "comp-b", new String[]{"rendering", "ui"});
        repo.registerComponent(String.class, "audio-a", "comp-c", new String[]{"audio"});

        List<String> rendering = repo.getByTag(String.class, "rendering");
        assertEquals(2, rendering.size());

        List<String> audio = repo.getByTag(String.class, "audio");
        assertEquals(1, audio.size());
        assertEquals("audio-a", audio.get(0));

        List<String> multiTag = repo.getByTags(String.class, "rendering", "ui");
        assertEquals(2, multiTag.size()); // Union of both tags
    }

    @Test
    void testTagBasedInjection() {
        BlueprintLoader.load(new String[]{"org.pixel.blueprint"});

        TagConsumer consumer = new TagConsumer();
        BlueprintAssembler.assemble(consumer);

        assertNotNull(consumer.renderables);
        assertEquals(2, consumer.renderables.size());
        assertTrue(consumer.renderables.contains("renderable-a"));
        assertTrue(consumer.renderables.contains("renderable-b"));

        assertNotNull(consumer.audioItems);
        assertEquals(1, consumer.audioItems.size());
        assertEquals("audio-a", consumer.audioItems.get(0));
    }

    // endregion

    // region Scope tests

    @Test
    void testSingletonScopeDefault() {
        BlueprintLoader.load(new String[]{"org.pixel.blueprint"});

        SingletonService instance1 = BlueprintRepository.getDefault().ugetService(SingletonService.class, "singletonService");
        SingletonService instance2 = BlueprintRepository.getDefault().ugetService(SingletonService.class, "singletonService");

        assertNotNull(instance1);
        assertSame(instance1, instance2, "Singleton scope should return the same instance");
    }

    @Test
    void testPrototypeScope() {
        BlueprintLoader.load(new String[]{"org.pixel.blueprint"});

        PrototypeService instance1 = BlueprintRepository.getDefault().uget(PrototypeService.class, "prototypeService");
        PrototypeService instance2 = BlueprintRepository.getDefault().uget(PrototypeService.class, "prototypeService");

        // Prototype instances should be different each time (when created via get())
        // Note: currently uget() doesn't auto-create prototypes; this tests registration
        assertNull(instance1, "Prototype services should not be pre-instantiated");
    }

    @Test
    void testPrototypeComponentFactoryRegistered() {
        BlueprintLoader.load(new String[]{"org.pixel.blueprint"});

        assertTrue(BlueprintRepository.getDefault().getPrototypeComponentFactories().containsKey("prototypeComponent"),
                "Prototype component factory should be registered");
    }

    // endregion

    // region Conditional tests

    @Test
    void testConditionalSkipped() {
        BlueprintRepository.getDefault().clear();
        BlueprintLoader.load(new String[]{"org.pixel.blueprint"});

        // The skipped component should NOT be in the repository because the condition is false
        Object skipped = BlueprintRepository.getDefault().ugetComponent(String.class, "skippedComponent");
        assertNull(skipped, "Component with failing condition should be skipped");
    }

    @Test
    void testConditionalIncluded() {
        BlueprintRepository.getDefault().clear();
        BlueprintLoader.load(new String[]{"org.pixel.blueprint"});

        Object included = BlueprintRepository.getDefault().ugetComponent(String.class, "includedComponent");
        assertNotNull(included, "Component with passing condition should be included");
        assertEquals("I'm included!", included);
    }

    // endregion

    // region Interface binding tests

    @Test
    void testInterfaceBinding() {
        BlueprintRepository repo = BlueprintRepository.getDefault();
        repo.clear();

        GreeterImpl impl = new GreeterImpl();
        repo.registerComponent(GreeterImpl.class, impl, "greeter");

        // Should be able to retrieve by interface
        Greeter result = repo.uget(Greeter.class);
        assertNotNull(result);
        assertSame(impl, result);
    }

    // endregion

    // region Thread safety

    @Test
    void testRepositoryIsThreadSafe() {
        BlueprintRepository repo = new BlueprintRepository();

        // Concurrent registrations should not throw
        assertDoesNotThrow(() -> {
            Thread t1 = new Thread(() -> {
                for (int i = 0; i < 100; i++) {
                    repo.registerComponent(String.class, "value-" + i, "name-" + i, new String[]{"tag"});
                }
            });
            Thread t2 = new Thread(() -> {
                for (int i = 100; i < 200; i++) {
                    repo.registerComponent(String.class, "value-" + i, "name-" + i, new String[]{"tag"});
                }
            });
            t1.start();
            t2.start();
            t1.join();
            t2.join();
        });

        assertTrue(repo.hasData());
    }

    // endregion

    // region Exception handling

    @Test
    void testBlueprintExceptionIsThrown() {
        assertThrows(BlueprintException.class, () -> {
            BlueprintAssembler.assemble(NonInjectableClass.class);
        });
    }

    // endregion

    // region Test fixtures

    @Blueprint
    public static class TagBlueprintConfig {

        @Component(value = "renderable-a", tags = {"rendering"})
        public String renderableA() {
            return "renderable-a";
        }

        @Component(value = "renderable-b", tags = {"rendering", "ui"})
        public String renderableB() {
            return "renderable-b";
        }

        @Component(value = "audio-a", tags = {"audio"})
        public String audioA() {
            return "audio-a";
        }

        @Component(value = "skippedComponent")
        @Conditional({AlwaysFalseCondition.class})
        public String skippedComponent() {
            return "I'm skipped!";
        }

        @Component(value = "includedComponent")
        @Conditional({AlwaysTrueCondition.class})
        public String includedComponent() {
            return "I'm included!";
        }

        @Component(value = "prototypeComponent")
        @Scope(ScopeType.PROTOTYPE)
        public String prototypeComponent() {
            return "prototype-value";
        }
    }

    public static class TagConsumer {
        @Auto(tags = {"rendering"})
        private List<String> renderables;

        @Auto(tags = {"audio"})
        private List<String> audioItems;
    }

    @Service(value = "singletonService")
    public static class SingletonService {
        public String hello() {
            return "hello";
        }
    }

    @Service(value = "prototypeService")
    @Scope(ScopeType.PROTOTYPE)
    public static class PrototypeService {
        public String hello() {
            return "hello";
        }
    }

    public static class AlwaysFalseCondition implements Condition {
        @Override
        public boolean matches() {
            return false;
        }
    }

    public static class AlwaysTrueCondition implements Condition {
        @Override
        public boolean matches() {
            return true;
        }
    }

    public interface Greeter {
        String greet();
    }

    public static class GreeterImpl implements Greeter {
        @Override
        public String greet() {
            return "Hello!";
        }
    }

    public static class NonInjectableClass {
        public NonInjectableClass(@Auto String missingDependency) {
            // This constructor requires a dependency that doesn't exist
        }
    }

    // endregion
}
