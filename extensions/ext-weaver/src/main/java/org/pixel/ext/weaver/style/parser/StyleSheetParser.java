package org.pixel.ext.weaver.style.parser;

import org.pixel.commons.data.Pair;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.ext.weaver.style.CssRule;
import org.pixel.ext.weaver.style.StyleSheet;
import org.pixel.ext.weaver.style.resource.FontResource;
import org.pixel.ext.weaver.style.resource.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A "from-scratch" simple CSS parser.
 * This class acts as a factory, turning a raw CSS string into a structured Stylesheet object.
 * Supports:
 * - Descendant selectors (e.g., "panel button")
 * - ID, Class, and pseudo-class selectors (e.g., "#id", ".class", ":hover")
 * - Variables defined in a :root { ... } block.
 * - Comments (/* ... *\/)
 */
public class StyleSheetParser {

    private static final Logger LOG = LoggerFactory.getLogger(StyleSheetParser.class);

    private final AtomicInteger orderCounter = new AtomicInteger(0);

    public StyleSheet parse(String css) {
        List<CssRule> rules = new ArrayList<>();
        Map<String, String> variables = new HashMap<>();
        List<Resource> resources = new ArrayList<>();

        // 1. Pre-process: remove comments and newlines for easier parsing
        String processed = css
                .replaceAll("/\\*.*?\\*/", "") // Remove comments
                .replaceAll("\\r\\n|\\n", " "); // Remove newlines

        // 2. Find all rule blocks
        int blockStart;
        while ((blockStart = processed.indexOf('{')) != -1) {
            int blockEnd = processed.indexOf('}', blockStart);
            if (blockEnd == -1) {
                // Malformed CSS, unclosed block
                LOG.warn("Malformed CSS: unclosed block starting at position " + blockStart);
                break;
            }

            // 3. Extract selector and properties
            String selector = processed.substring(0, blockStart).trim();
            String properties = processed.substring(blockStart + 1, blockEnd).trim();

            // 4. Process the parts
            if (selector.equalsIgnoreCase(":root")) {
                // This is a variable block
                variables.putAll(parseDeclarations(properties));
            } else if (selector.equalsIgnoreCase(":res")) {
                // TODO: Whenever we support more resource types this could probably move elsewhere
                // This is a resource block
                var resDeclarations = parseMultipleDeclarations(properties);
                for (var entry : resDeclarations) {
                    var resourceType = entry.getA();
                    var params = entry.getB().split(",");
                    if (resourceType.equalsIgnoreCase("font")) {
                        // Expected format: MyFont, /path/to/font.ttf, 16 (optional, default 32)
                        if (params.length >= 2) {
                            var name = params[0].trim();
                            var path = params[1].trim();
                            var size = params.length >= 3 ? Integer.parseInt(params[2].trim()) : 32;
                            resources.add(FontResource.builder()
                                    .name(name)
                                    .path(path)
                                    .size(size)
                                    .build());
                        }
                    }
                }
            } else {
                // This is a normal rule block
                Map<String, String> declarations = parseDeclarations(properties);
                int specificity = calculateSpecificity(selector);
                CssRule rule = new CssRule(selector, declarations, specificity, orderCounter.getAndIncrement());
                rules.add(rule);
            }

            // 5. Move to the next block
            processed = processed.substring(blockEnd + 1);
        }

        LOG.debug("Parsed stylesheet: {0} rules, {1} variables, {2} resources.",
                rules.size(), variables.size(), resources.size());

        return new StyleSheet(rules, variables, resources);
    }

    /**
     * Parses the inner text of a rule block (e.g., "color: white; font-size: 10px")
     * into a key-value map.
     */
    private Map<String, String> parseDeclarations(String properties) {
        Map<String, String> declarations = new HashMap<>();
        if (properties.isEmpty()) {
            return declarations;
        }

        String[] pairs = properties.split(";");
        for (String pair : pairs) {
            String[] parts = pair.split(":", 2); // Split on the *first* colon only
            if (parts.length == 2) {
                String prop = parts[0].trim();
                String value = parts[1].trim();
                if (!prop.isEmpty() && !value.isEmpty()) {
                    declarations.put(prop, value);
                }
            }
        }
        return declarations;
    }

    private List<Pair<String, String>> parseMultipleDeclarations(String properties) {
        List<Pair<String, String>> declarations = new ArrayList<>();
        if (properties.isEmpty()) {
            return declarations;
        }

        String[] pairs = properties.split(";");
        for (String pair : pairs) {
            String[] parts = pair.split(":", 2); // Split on the *first* colon only
            if (parts.length == 2) {
                String prop = parts[0].trim();
                String value = parts[1].trim();
                if (!prop.isEmpty() && !value.isEmpty()) {
                    declarations.add(new Pair<>(prop, value));
                }
            }
        }
        return declarations;
    }

    /**
     * Calculates a simple specificity score for a selector.
     * - ID (#id): 100 points
     * - Class (.class) or Pseudo-class (:hover): 10 points
     * - Type (button, panel): 1 point
     */
    private int calculateSpecificity(String selector) {
        int score = 0;

        // Split by spaces to handle descendant selectors
        for (String part : selector.split(" ")) {
            if (part.isEmpty()) continue;

            // 1. Count IDs
            score += 100 * (part.length() - part.replace("#", "").length());

            // 2. Count Classes and Pseudo-classes
            score += 10 * (part.length() - part.replace(".", "").length());
            score += 10 * (part.length() - part.replace(":", "").length());

            // 3. Count Type (if it starts with a letter)
            // This is a simple check; it assumes a part is either a type
            // or starts with a sigil (#, ., :)
            if (Character.isLetter(part.charAt(0))) {
                score += 1;
            }
        }
        return score;
    }
}
