package org.pixel.ext.weaver.style;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pixel.commons.Color;
import org.pixel.ext.weaver.style.parser.StyleSheetParser;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class StyleEngineTest {

    private StyleEngine styleEngine;
    private StyleSheetParser parser;

    @BeforeEach
    void setUp() {
        styleEngine = new StyleEngine();
        parser = new StyleSheetParser();
    }

    @Test
    void testBasicStyleApplication() {
        // Create a simple stylesheet
        String css = """
                button {
                    background-color: #FF0000;
                    font-size: 14px;
                }
                """;

        StyleSheet sheet = parser.parse(css);
        styleEngine.loadStyleSheet(sheet);

        // Create a mock button widget
        MockStyleable button = new MockStyleable("button");

        // Get computed style
        Style style = styleEngine.getComputedStyle(button);

        // Verify the properties were applied and parsed
        assertNotNull(style);
        assertColorEquals(Color.fromString("#FF0000"), style.get(StyleProperties.BACKGROUND_COLOR));
        assertEquals(14.0f, style.<Float>get(StyleProperties.FONT_SIZE));
    }

    @Test
    void testClassSelectorMatching() {
        String css = """
                .primary {
                    background-color: #0000FF;
                }
                """;

        StyleSheet sheet = parser.parse(css);
        styleEngine.loadStyleSheet(sheet);

        MockStyleable button = new MockStyleable("button");
        button.addStyleClass("primary");

        Style style = styleEngine.getComputedStyle(button);

        assertColorEquals(Color.fromString("#0000FF"), style.get(StyleProperties.BACKGROUND_COLOR));
    }

    @Test
    void testIdSelectorMatching() {
        String css = """
                #submit-btn {
                    background-color: #00FF00;
                }
                """;

        StyleSheet sheet = parser.parse(css);
        styleEngine.loadStyleSheet(sheet);

        MockStyleable button = new MockStyleable("button");
        button.setStyleId("submit-btn");

        Style style = styleEngine.getComputedStyle(button);

        assertColorEquals(Color.fromString("#00FF00"), style.get(StyleProperties.BACKGROUND_COLOR));
    }

    @Test
    void testPseudoClassMatching() {
        String css = """
                button:hover {
                    background-color: #FFFF00;
                }
                """;

        StyleSheet sheet = parser.parse(css);
        styleEngine.loadStyleSheet(sheet);

        MockStyleable button = new MockStyleable("button");
        button.addPseudoClass("hover");

        Style style = styleEngine.getComputedStyle(button);

        assertColorEquals(Color.fromString("#FFFF00"), style.get(StyleProperties.BACKGROUND_COLOR));
    }

    @Test
    void testDescendantSelectorMatching() {
        String css = """
                panel button {
                    background-color: #FF00FF;
                }
                """;

        StyleSheet sheet = parser.parse(css);
        styleEngine.loadStyleSheet(sheet);

        MockStyleable panel = new MockStyleable("panel");
        MockStyleable button = new MockStyleable("button");
        button.setParent(panel);

        Style style = styleEngine.getComputedStyle(button);

        assertColorEquals(Color.fromString("#FF00FF"), style.get(StyleProperties.BACKGROUND_COLOR));
    }

    @Test
    void testSpecificityCascade() {
        // Higher specificity should override lower specificity
        String css = """
                button {
                    background-color: #FF0000;
                }
                
                .primary {
                    background-color: #0000FF;
                }
                
                #submit-btn {
                    background-color: #00FF00;
                }
                """;

        StyleSheet sheet = parser.parse(css);
        styleEngine.loadStyleSheet(sheet);

        MockStyleable button = new MockStyleable("button");
        button.addStyleClass("primary");
        button.setStyleId("submit-btn");

        Style style = styleEngine.getComputedStyle(button);

        // ID selector (#submit-btn) has highest specificity, so green should win
        assertColorEquals(Color.fromString("#00FF00"), style.get(StyleProperties.BACKGROUND_COLOR));
    }

    @Test
    void testVariableResolution() {
        String css = """
                :root {
                    --main-color: #FF0000;
                }
                
                button {
                    background-color: var(--main-color);
                }
                """;

        StyleSheet sheet = parser.parse(css);
        styleEngine.loadStyleSheet(sheet);

        MockStyleable button = new MockStyleable("button");

        Style style = styleEngine.getComputedStyle(button);

        assertColorEquals(Color.fromString("#FF0000"), style.get(StyleProperties.BACKGROUND_COLOR));
    }

    @Test
    void testInheritance() {
        String css = """
                :root {
                    --main-color: #FF0000;
                }
                
                panel {
                    color: var(--main-color);
                    font-size: 16px;
                }
                
                panel.specific {
                    font-size: 14px;
                }
                """;

        StyleSheet sheet = parser.parse(css);
        styleEngine.loadStyleSheet(sheet);

        MockStyleable panel = new MockStyleable("panel");
        panel.addStyleClass("specific");

        Style style = styleEngine.getComputedStyle(panel);
        // color should be inherited from panel
        assertColorEquals(Color.fromString("#FF0000"), style.get(StyleProperties.COLOR));
        // font-size should be from more specific rule
        assertEquals(14.0f, style.<Float>get(StyleProperties.FONT_SIZE));
    }

    @Test
    void testEntityInheritance() {
        String css = """
                panel {
                    color: #F8F8F8;
                    font-size: 16px;
                }
                
                panel button {
                    /* override font-size for buttons in panels */
                    font-size: 14px;
                }
                """;

        StyleSheet sheet = parser.parse(css);
        styleEngine.loadStyleSheet(sheet);

        MockStyleable panel = new MockStyleable("panel");
        MockStyleable button = new MockStyleable("button");
        button.setParent(panel);

        Style buttonStyle = styleEngine.getComputedStyle(button);

        // Button should inherit color from panel but have its own font-size
        assertColorEquals(Color.fromString("#F8F8F8"), buttonStyle.get(StyleProperties.COLOR));
        assertEquals(14.0f, buttonStyle.<Float>get(StyleProperties.FONT_SIZE));
    }

    @Test
    void testDuplicateSelectors() {
        String css = """
                button {
                    background-color: #FF0000;
                }
                
                button {
                    /* This should override the previous background-color */
                    background-color: #00FF00;
                }
                """;

        StyleSheet sheet = parser.parse(css);

        MockStyleable button = new MockStyleable("button");
        styleEngine.loadStyleSheet(sheet);

        assertEquals(2, sheet.rules().size(), "There should be two rules for .button selector");
        assertColorEquals(Color.fromString("#00FF00"),
                styleEngine.getComputedStyle(button).get(StyleProperties.BACKGROUND_COLOR));

        String anotherCss = """
                button {
                    background-color: #0000FF;
                }
                """;

        StyleSheet anotherSheet = parser.parse(anotherCss);
        styleEngine.loadStyleSheet(anotherSheet);

        assertColorEquals(Color.fromString("#0000FF"),
                styleEngine.getComputedStyle(button).get(StyleProperties.BACKGROUND_COLOR));
    }

    @Test
    void testCaching() {
        String css = """
                button {
                    background-color: #FF0000;
                }
                """;

        StyleSheet sheet = parser.parse(css);
        styleEngine.loadStyleSheet(sheet);

        MockStyleable button = new MockStyleable("button");

        Style style1 = styleEngine.getComputedStyle(button);
        Style style2 = styleEngine.getComputedStyle(button);

        // Should return the exact same cached instance
        assertSame(style1, style2);
    }

    @Test
    void testCacheInvalidationOnNewStyleSheet() {
        String css1 = """
                button {
                    background-color: #FF0000;
                }
                """;

        StyleSheet sheet1 = parser.parse(css1);
        styleEngine.loadStyleSheet(sheet1);

        MockStyleable button = new MockStyleable("button");
        Style style1 = styleEngine.getComputedStyle(button);

        assertColorEquals(Color.fromString("#FF0000"), style1.get(StyleProperties.BACKGROUND_COLOR));

        // Load a new stylesheet - should invalidate cache
        String css2 = """
                button {
                    background-color: #0000FF;
                }
                """;

        StyleSheet sheet2 = parser.parse(css2);
        styleEngine.loadStyleSheet(sheet2);

        Style style2 = styleEngine.getComputedStyle(button);

        // New style should apply (blue overrides red)
        assertColorEquals(Color.fromString("#0000FF"), style2.get(StyleProperties.BACKGROUND_COLOR));
        // Should be a different instance due to cache invalidation
        assertNotSame(style1, style2);
    }

    @Test
    void testEmptyStyleForNullWidget() {
        Style style = styleEngine.getComputedStyle(null);
        assertSame(Style.EMPTY, style);
    }

    @Test
    void testComplexDescendantSelector() {
        // "panel.dark button" means: button inside a panel that has class "dark"
        String css = """
                panel.dark button {
                    background-color: #333333;
                }
                """;

        StyleSheet sheet = parser.parse(css);
        styleEngine.loadStyleSheet(sheet);

        MockStyleable panel = new MockStyleable("panel");
        panel.addStyleClass("dark");

        MockStyleable button = new MockStyleable("button");
        button.setParent(panel);

        Style style = styleEngine.getComputedStyle(button);

        assertColorEquals(Color.fromString("#333333"), style.get(StyleProperties.BACKGROUND_COLOR));
    }

    @Test
    void testDescendantSelectorWithSpaces() {
        // Test that "panel .dark button" works (with space before .dark)
        // This means: button is descendant of .dark, which is descendant of panel
        String css = """
                panel .dark button {
                    background-color: #444444;
                }
                """;

        StyleSheet sheet = parser.parse(css);
        styleEngine.loadStyleSheet(sheet);

        MockStyleable panel = new MockStyleable("panel");
        MockStyleable darkContainer = new MockStyleable("div");
        darkContainer.addStyleClass("dark");
        darkContainer.setParent(panel);

        MockStyleable button = new MockStyleable("button");
        button.setParent(darkContainer);

        Style style = styleEngine.getComputedStyle(button);

        assertColorEquals(Color.fromString("#444444"), style.get(StyleProperties.BACKGROUND_COLOR));
    }

    @Test
    void testComplexSelectorDistinction() {
        // Verify that "panel.dark button" and "panel .dark button" are different
        String css = """
                panel.dark button {
                    background-color: #111111;
                }
                """;

        StyleSheet sheet = parser.parse(css);
        styleEngine.loadStyleSheet(sheet);

        // Case 1: panel.dark button - should match (panel has dark class, button is child)
        MockStyleable panelWithDark = new MockStyleable("panel");
        panelWithDark.addStyleClass("dark");
        MockStyleable buttonInPanelDark = new MockStyleable("button");
        buttonInPanelDark.setParent(panelWithDark);

        Style style1 = styleEngine.getComputedStyle(buttonInPanelDark);
        assertColorEquals(Color.fromString("#111111"), style1.get(StyleProperties.BACKGROUND_COLOR));

        // Case 2: panel > div.dark > button - should NOT match (panel doesn't have dark class)
        MockStyleable panelPlain = new MockStyleable("panel");
        MockStyleable divWithDark = new MockStyleable("div");
        divWithDark.addStyleClass("dark");
        divWithDark.setParent(panelPlain);
        MockStyleable buttonInDiv = new MockStyleable("button");
        buttonInDiv.setParent(divWithDark);

        Style style2 = styleEngine.getComputedStyle(buttonInDiv);
        assertNull(style2.get(StyleProperties.BACKGROUND_COLOR), "Should not match panel.dark button when dark is on child, not panel");
    }

    @Test
    void testMultipleStyleSheets() {
        String css1 = """
                button {
                    background-color: #FF0000;
                    font-size: 14px;
                }
                """;

        String css2 = """
                button {
                    background-color: #0000FF;
                }
                """;

        styleEngine.loadStyleSheet(parser.parse(css1));
        styleEngine.loadStyleSheet(parser.parse(css2));

        MockStyleable button = new MockStyleable("button");
        Style style = styleEngine.getComputedStyle(button);

        // Second stylesheet should override background-color
        assertColorEquals(Color.fromString("#0000FF"), style.get(StyleProperties.BACKGROUND_COLOR));
        // But font-size from first stylesheet should remain
        assertEquals(14.0f, style.<Float>get(StyleProperties.FONT_SIZE));
    }

    /**
     * Helper method to compare Color objects by their RGBA values since
     * Color class doesn't implement equals().
     */
    private void assertColorEquals(Color expected, Color actual) {
        assertNotNull(actual, "Color should not be null");
        assertEquals(expected.getRed(), actual.getRed(), 0.01f, "Red component mismatch");
        assertEquals(expected.getGreen(), actual.getGreen(), 0.01f, "Green component mismatch");
        assertEquals(expected.getBlue(), actual.getBlue(), 0.01f, "Blue component mismatch");
        assertEquals(expected.getAlpha(), actual.getAlpha(), 0.01f, "Alpha component mismatch");
    }

    // Mock implementation of Styleable for testing
    private static class MockStyleable implements Styleable {
        private final String type;
        private String id;
        private final Set<String> classes = new HashSet<>();
        private final Set<String> pseudoClasses = new HashSet<>();
        private Styleable parent;
        private final List<Styleable> children = new ArrayList<>();

        public MockStyleable(String type) {
            this.type = type;
        }

        public void setStyleId(String id) {
            this.id = id;
        }

        public void addStyleClass(String className) {
            this.classes.add(className);
        }

        public void addPseudoClass(String pseudoClass) {
            this.pseudoClasses.add(pseudoClass);
        }

        public void setParent(Styleable parent) {
            this.parent = parent;
        }

        @Override
        public String getStyleType() {
            return type;
        }

        @Override
        public String getStyleId() {
            return id;
        }

        @Override
        public Set<String> getClasses() {
            return classes;
        }

        @Override
        public Set<String> getPseudoClasses() {
            return pseudoClasses;
        }

        @Override
        public Styleable getStyleableParent() {
            return parent;
        }

        @Override
        public List<Styleable> getStyleableChildren() {
            return children;
        }
    }
}
