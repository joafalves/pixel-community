package org.pixel.ext.weaver.style;

import org.pixel.ext.weaver.style.resource.Resource;

import java.util.List;
import java.util.Map;

/**
 * A data object representing a single, fully parsed stylesheet.
 * It contains all the rules and variables found by the StylesheetParser.
 */
public record StyleSheet(
        List<CssRule> rules,
        Map<String, String> variables,
        List<Resource> resources) {

}