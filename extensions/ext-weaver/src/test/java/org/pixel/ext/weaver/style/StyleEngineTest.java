package org.pixel.ext.weaver.style;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.ext.weaver.WeaverContext;
import org.pixel.ext.weaver.style.parser.StyleSheetParser;
import org.pixel.ext.weaver.style.property.StyleProperties;
import org.pixel.ext.weaver.widget.Widget;

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
        TestWidget button = new TestWidget("button");

        // Get computed style
        Style style = styleEngine.getComputedStyle(button);

        // Verify the properties were applied and parsed
        assertNotNull(style);
        assertColorEquals(Color.fromString("#FF0000"), style.get(StyleProperties.BACKGROUND_COLOR));
        assertEquals(14.0f, style.get(StyleProperties.FONT_SIZE).value());
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

        TestWidget button = new TestWidget("button");
        button.addClass("primary");

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

        TestWidget button = new TestWidget("button");
        button.setId("submit-btn");

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

        TestWidget button = new TestWidget("button");
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

        TestWidget panel = new TestWidget("panel");
        TestWidget button = new TestWidget("button");
        panel.addChild(button);

        Style style = styleEngine.getComputedStyle(button);

        assertColorEquals(Color.fromString("#FF00FF"), style.get(StyleProperties.BACKGROUND_COLOR));
    }

    @Test
    void testSpecificityCascade() {
        // Higher specificity should override lower specificity
        String css = """
                .primary {
                    color: #FF0000;
                    background-color: #0000FF;
                }
                
                #submit-btn {
                    background-color: #00FF00;
                }
                """;

        StyleSheet sheet = parser.parse(css);
        styleEngine.loadStyleSheet(sheet);

        TestWidget button = new TestWidget("button");
        button.setId("submit-btn");
        button.addClass("primary");

        Style style = styleEngine.getComputedStyle(button);

        // ID selector (#submit-btn) has highest specificity, so green should win
        assertColorEquals(Color.fromString("#00FF00"), style.get(StyleProperties.BACKGROUND_COLOR));
        assertColorEquals(Color.fromString("#FF0000"), style.get(StyleProperties.COLOR));
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

        TestWidget button = new TestWidget("button");

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

        TestWidget panel = new TestWidget("panel");
        panel.addClass("specific");

        Style style = styleEngine.getComputedStyle(panel);
        // color should be inherited from panel
        assertColorEquals(Color.fromString("#FF0000"), style.get(StyleProperties.COLOR));
        // font-size should be from more specific rule
        assertEquals(14.0f, style.get(StyleProperties.FONT_SIZE).value());
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

        TestWidget panel = new TestWidget("panel");
        TestWidget button = new TestWidget("button");
        panel.addChild(button);

        Style buttonStyle = styleEngine.getComputedStyle(button);

        // Button should inherit color from panel but have its own font-size
        assertColorEquals(Color.fromString("#F8F8F8"), buttonStyle.get(StyleProperties.COLOR));
        assertEquals(14.0f, buttonStyle.get(StyleProperties.FONT_SIZE).value());
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

        TestWidget button = new TestWidget("button");
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

        TestWidget button = new TestWidget("button");

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

        TestWidget button = new TestWidget("button");
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

        TestWidget panel = new TestWidget("panel");
        panel.addClass("dark");

        TestWidget button = new TestWidget("button");
        panel.addChild(button);

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

        TestWidget panel = new TestWidget("panel");
        TestWidget darkContainer = new TestWidget("div");
        darkContainer.addClass("dark");
        panel.addChild(darkContainer);

        TestWidget button = new TestWidget("button");
        darkContainer.addChild(button);

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
        TestWidget panelWithDark = new TestWidget("panel");
        panelWithDark.addClass("dark");
        TestWidget buttonInPanelDark = new TestWidget("button");
        panelWithDark.addChild(buttonInPanelDark);

        Style style1 = styleEngine.getComputedStyle(buttonInPanelDark);
        assertColorEquals(Color.fromString("#111111"), style1.get(StyleProperties.BACKGROUND_COLOR));

        // Case 2: panel > div.dark > button - should NOT match (panel doesn't have dark class)
        TestWidget panelPlain = new TestWidget("panel");
        TestWidget divWithDark = new TestWidget("div");
        divWithDark.addClass("dark");
        panelPlain.addChild(divWithDark);
        TestWidget buttonInDiv = new TestWidget("button");
        divWithDark.addChild(buttonInDiv);

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

        TestWidget button = new TestWidget("button");
        Style style = styleEngine.getComputedStyle(button);

        // Second stylesheet should override background-color
        assertColorEquals(Color.fromString("#0000FF"), style.get(StyleProperties.BACKGROUND_COLOR));
        // But font-size from first stylesheet should remain
        assertEquals(14.0f, style.get(StyleProperties.FONT_SIZE).value());
    }

    @Test
    void testWidgetInheritance() {
        String css = """
                panel {
                    background-color: #CCCCCC;
                    width: 800px;
                    height: 600px;
                }
                
                button {
                    font-size: 14px;
                }
                """;

        styleEngine.loadStyleSheet(parser.parse(css));

        TestWidget panel = new TestWidget("panel");
        TestWidget button = new TestWidget("button");
        panel.addChild(button);

        Style buttonStyle = styleEngine.getComputedStyle(button);

        // Button should inherit nothing from panel since no inheritance is defined for these properties
        assertNull(buttonStyle.getRaw(StyleProperties.BACKGROUND_COLOR.name()), "Button should not inherit background-color from panel");
        assertNull(buttonStyle.getRaw(StyleProperties.WIDTH.name()), "Button should not inherit width from panel");
        assertNull(buttonStyle.getRaw(StyleProperties.HEIGHT.name()), "Button should not inherit height from panel");
        // But button should have its own font-size
        assertNotNull(buttonStyle.getRaw(StyleProperties.FONT_SIZE.name()), "Button should have font-size from button rule");
        assertEquals(14.0f, buttonStyle.get(StyleProperties.FONT_SIZE).value());
    }

    @Test
    void testWidgetInlineStyling() {
        String css = """
                button {
                    background-color: #FF0000;
                }
                """;

        styleEngine.loadStyleSheet(parser.parse(css));

        TestWidget button = new TestWidget("button");
        button.setInlineStyle(StyleProperties.BACKGROUND_COLOR, Color.fromString("#00FF00"));

        Style style = styleEngine.getComputedStyle(button);

        // Inline style should take precedence
        assertColorEquals(Color.fromString("#00FF00"), style.get(StyleProperties.BACKGROUND_COLOR));
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

    static class TestWidget extends Widget {

        private final String styleType;

        public TestWidget(String styleType) {
            this.styleType = styleType;
        }

        @Override
        public void update(DeltaTime delta, WeaverContext ctx) {

        }

        @Override
        public void draw(DeltaTime delta, WeaverContext ctx) {

        }

        @Override
        protected void drawContent(DeltaTime delta, WeaverContext ctx) {

        }

        @Override
        public String getStyleType() {
            return styleType;
        }
    }
}
