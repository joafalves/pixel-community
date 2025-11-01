package org.pixel.ext.weaver.style;

import java.util.Map;

/**
 * @param order For tie-breaking when specificity is equal
 */
public record CssRule(String selector, Map<String, String> declarations, int specificity, int order) {
}
