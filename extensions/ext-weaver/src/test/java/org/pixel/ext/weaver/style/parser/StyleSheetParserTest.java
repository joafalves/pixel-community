package org.pixel.ext.weaver.style.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pixel.ext.weaver.style.CssRule;
import org.pixel.ext.weaver.style.StyleSheet;
import org.pixel.ext.weaver.style.resource.FontResource;

import static org.junit.jupiter.api.Assertions.*;

class StyleSheetParserTest {

    private StyleSheetParser parser;

    @BeforeEach
    void setUp() {
        parser = new StyleSheetParser();
    }

    @Test
    public void testParse() {
        String styleSheet = """
                        .button {
                            color: #FF0000;
                            font-size: 14px;
                            }
                
                        .panel {
                            background-color: #00FF00;
                            padding: 10px;
                        }
                """;

        StyleSheet result = parser.parse(styleSheet);

        // Verify the stylesheet has 2 rules and no variables
        assertNotNull(result);
        assertEquals(2, result.rules().size());
        assertTrue(result.variables().isEmpty());

        // Verify the .button rule
        CssRule buttonRule = result.rules().get(0);
        assertEquals(".button", buttonRule.selector());
        assertEquals(2, buttonRule.declarations().size());
        assertEquals("#FF0000", buttonRule.declarations().get("color"));
        assertEquals("14px", buttonRule.declarations().get("font-size"));
        assertEquals(10, buttonRule.specificity()); // Class selector = 10 points
        assertEquals(0, buttonRule.order());

        // Verify the .panel rule
        CssRule panelRule = result.rules().get(1);
        assertEquals(".panel", panelRule.selector());
        assertEquals(2, panelRule.declarations().size());
        assertEquals("#00FF00", panelRule.declarations().get("background-color"));
        assertEquals("10px", panelRule.declarations().get("padding"));
        assertEquals(10, panelRule.specificity()); // Class selector = 10 points
        assertEquals(1, panelRule.order());
    }

    @Test
    public void testParseWithVariables() {
        String styleSheet = """
                    :root {
                        --main-color: #123456;
                        --padding-size: 15px;
                    }
                
                    .container {
                        color: var(--main-color);
                        padding: var(--padding-size);
                    }
                """;

        StyleSheet result = parser.parse(styleSheet);

        // Verify the stylesheet has 1 rule and 2 variables
        assertNotNull(result);
        assertEquals(1, result.rules().size());
        assertEquals(2, result.variables().size());
        assertEquals("#123456", result.variables().get("--main-color"));
        assertEquals("15px", result.variables().get("--padding-size"));

        // Verify the .container rule
        CssRule containerRule = result.rules().get(0);
        assertEquals(".container", containerRule.selector());
        assertEquals(2, containerRule.declarations().size());
        assertEquals("var(--main-color)", containerRule.declarations().get("color"));
        assertEquals("var(--padding-size)", containerRule.declarations().get("padding"));
        assertEquals(10, containerRule.specificity()); // Class selector = 10 points
        assertEquals(0, containerRule.order());
    }

    @Test
    public void testParseWithResources() {
        String css = """
                :res {
                    font: roboto, __weaver__/font/roboto-regular.ttf, 32;
                    font: roboto-medium, __weaver__/font/roboto-medium.ttf, 32;
                }
                """;

        StyleSheet sheet = parser.parse(css);

        assertEquals(2, sheet.resources().size());
        assertTrue(sheet.resources().stream().anyMatch(res ->
                res instanceof FontResource fontRes &&
                        fontRes.getName().equals("roboto") &&
                        fontRes.getPath().equals("__weaver__/font/roboto-regular.ttf") &&
                        fontRes.getSize() == 32
        ), "Roboto font resource should be loaded");
    }
}