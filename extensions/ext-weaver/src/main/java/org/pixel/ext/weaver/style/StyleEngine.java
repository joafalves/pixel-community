package org.pixel.ext.weaver.style;

import org.pixel.commons.Color;
import org.pixel.commons.data.Pair;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;

import java.util.*;

public class StyleEngine {

    private static final Logger LOG = LoggerFactory.getLogger(StyleEngine.class);
    private static final Set<String> INHERITABLE_PROPERTIES = Set.of(
            // Text & Font Properties
            StyleProperties.COLOR.name(),
            StyleProperties.FONT_FAMILY.name(),
            StyleProperties.FONT_SIZE.name(),
            StyleProperties.FONT_WEIGHT.name(),
            StyleProperties.FONT_STYLE.name(),
            StyleProperties.LINE_HEIGHT.name(),
            StyleProperties.LETTER_SPACING.name(),
            StyleProperties.WORD_SPACING.name(),
            StyleProperties.TEXT_ALIGN.name(),
            StyleProperties.TEXT_INDENT.name(),
            StyleProperties.TEXT_TRANSFORM.name(),
            StyleProperties.WHITE_SPACE.name(),
            StyleProperties.DIRECTION.name(),

            // User Interface Properties
            StyleProperties.CURSOR.name(),
            StyleProperties.VISIBILITY.name(),
            StyleProperties.CARET_COLOR.name(),
            StyleProperties.POINTER_EVENTS.name(),
            StyleProperties.USER_SELECT.name()

            // Note: Table/List properties (border-collapse, list-style)
            // are omitted as they are not relevant for this type of UI.
    );

    private final List<StyleSheet> stylesheets = new ArrayList<>();
    private final Map<String, String> globalVariables = new HashMap<>();

    /**
     * The style cache is the most important performance feature.
     * We use WeakHashMap so that when a UIWidget is garbage collected,
     * its cached style is automatically removed.
     *
     * Key: Styleable widget
     * Value: A Pair of (Computed Style, Style Version)
     */
    private final Map<Styleable, Pair<Style, Integer>> styleCache = new WeakHashMap<>();

    /**
     * Clears all loaded stylesheets and caches.
     */
    public void clear() {
        this.stylesheets.clear();
        this.globalVariables.clear();
        this.styleCache.clear();
    }

    /**
     * Loads a new stylesheet into the engine.
     * This is a "heavy" operation, as it invalidates all caches.
     *
     * @param sheet The stylesheet, fully parsed by StylesheetParser.
     */
    public void loadStyleSheet(StyleSheet sheet) {
        this.stylesheets.add(sheet);
        this.globalVariables.putAll(sheet.variables());
        // A new stylesheet can change everything. Clear the cache.
        this.styleCache.clear();
    }

    /**
     * The main public method.
     * Gets the final, computed style for a widget.
     *
     * @param widget The widget to style.
     * @return A ComputedStyle object with native, parsed types.
     */
    public Style getComputedStyle(Styleable widget) {
        if (widget == null) {
            return Style.EMPTY; // Base case for recursion
        }

        // Check cache first
        int widgetStyleVersion = widget.getStyleVersion();
        Pair<Style, Integer> cacheEntry = styleCache.get(widget);
        if (cacheEntry != null && cacheEntry.getB() == widgetStyleVersion) {
            return cacheEntry.getA();
        }

        // Not in cache or outdated? Compute it.
        Style computed = computeStyle(widget);

        // Cache and return
        if (cacheEntry == null) {
            cacheEntry = new Pair<>(computed, widgetStyleVersion);
            styleCache.put(widget, cacheEntry);

        } else {
            cacheEntry.set(computed, widgetStyleVersion);
        }

        return computed;
    }

    /**
     * The core private method where the "magic" happens.
     */
    private Style computeStyle(Styleable widget) {
        // --- Get Parent Style ---
        // This recursive call is safe and fast due to the caching
        // in getComputedStyle(). We need this for inheritance.
        Style parentStyle = getComputedStyle(widget.getStyleableParent());

        // --- Find All Matching Rules ---
        List<CssRule> matchingRules = new ArrayList<>();
        for (StyleSheet sheet : stylesheets) {
            for (CssRule rule : sheet.rules()) {
                if (matches(rule.selector(), widget)) {
                    matchingRules.add(rule);
                }
            }
        }

        // --- The "Cascade" ---
        // Sort the rules by specificity, then by file order.
        // This is the "C" in CSS.
        matchingRules.sort(Comparator
                .comparingInt(CssRule::specificity)
                .thenComparingInt(CssRule::order));

        // --- Apply Rules to String Map ---
        // Apply in sorted order. Later rules (higher specificity)
        // will automatically overwrite earlier ones.
        Map<String, String> stringProperties = new HashMap<>();
        for (CssRule rule : matchingRules) {
            stringProperties.putAll(rule.declarations());
        }

        // --- Resolve & Parse to Native Types ---
        // This is the critical optimization. We parse strings to native types *once*, right here.
        Map<String, Object> nativeProperties = new HashMap<>();
        for (Map.Entry<String, String> entry : stringProperties.entrySet()) {
            String prop = entry.getKey();
            String value = entry.getValue();

            // 5a. Resolve variable (e.g., "var(--main-color)")
            String resolvedValue = resolveVariable(value);

            // 5b. Parse to native type (e.g., "#fff" -> Color object)
            Object nativeValue = parseValue(prop, resolvedValue);

            nativeProperties.put(prop, nativeValue);
        }

        // --- Apply Inline Styles ---
        // Inline styles have the highest specificity.
        Map<String, Object> inlineStyles = widget.getInlineStyleMap();
        for (Map.Entry<String, Object> entry : inlineStyles.entrySet()) {
            String prop = entry.getKey();
            Object value = entry.getValue();
            nativeProperties.put(prop, value);
        }

        // --- Apply Inheritance ---
        // Now, check for inheritable props that *weren't* set by any rule.
        for (String prop : INHERITABLE_PROPERTIES) {
            if (!nativeProperties.containsKey(prop)) {
                // Get the *native* value directly from the parent's style
                Object inheritedValue = parentStyle.getRaw(prop);
                if (inheritedValue != null) {
                    nativeProperties.put(prop, inheritedValue);
                }
            }
        }

        // --- Done! ---
        // Create the final, read-only ComputedStyle object.
        return new Style(nativeProperties);
    }

    /**
     * Resolves a 'var(--name)' string into its final value.
     */
    private String resolveVariable(String value) {
        if (value != null && value.startsWith("var(--") && value.endsWith(")")) {
            String varName = value.substring(4, value.length() - 1);
            // Return the global variable, or "transparent" as a safe fallback
            return this.globalVariables.getOrDefault(varName, "transparent");
        }
        return value;
    }

    /**
     * Routes a string value to the correct parser based on its property name.
     * Expands string CSS values into native Java types for performance and type safety.
     */
    private Object parseValue(String property, String value) {
        // TODO: Make this more elegant, perhaps using a registry or map of parsers.

        // Color properties
        if (property.equals(StyleProperties.BACKGROUND_COLOR.name()) ||
            property.equals(StyleProperties.COLOR.name()) ||
            property.equals(StyleProperties.BORDER_COLOR.name()) ||
            property.equals(StyleProperties.BORDER_TOP_COLOR.name()) ||
            property.equals(StyleProperties.BORDER_RIGHT_COLOR.name()) ||
            property.equals(StyleProperties.BORDER_BOTTOM_COLOR.name()) ||
            property.equals(StyleProperties.BORDER_LEFT_COLOR.name()) ||
            property.equals(StyleProperties.CARET_COLOR.name()) ||
            property.equals(StyleProperties.OUTLINE_COLOR.name())) {
            return Color.fromString(value);
        }

        // Dimension/size properties (with units like px, em, rem, %)
        if (property.equals(StyleProperties.BORDER_RADIUS.name()) ||
            property.equals(StyleProperties.FONT_SIZE.name()) ||
            property.equals(StyleProperties.WIDTH.name()) ||
            property.equals(StyleProperties.HEIGHT.name()) ||
            property.equals(StyleProperties.MIN_WIDTH.name()) ||
            property.equals(StyleProperties.MIN_HEIGHT.name()) ||
            property.equals(StyleProperties.MAX_WIDTH.name()) ||
            property.equals(StyleProperties.MAX_HEIGHT.name()) ||
            property.equals(StyleProperties.LINE_HEIGHT.name()) ||
            property.equals(StyleProperties.LETTER_SPACING.name()) ||
            property.equals(StyleProperties.WORD_SPACING.name()) ||
            property.equals(StyleProperties.TEXT_INDENT.name()) ||
            property.equals(StyleProperties.BORDER_WIDTH.name()) ||
            property.equals(StyleProperties.BORDER_TOP_WIDTH.name()) ||
            property.equals(StyleProperties.BORDER_RIGHT_WIDTH.name()) ||
            property.equals(StyleProperties.BORDER_BOTTOM_WIDTH.name()) ||
            property.equals(StyleProperties.BORDER_LEFT_WIDTH.name()) ||
            property.equals(StyleProperties.OUTLINE_WIDTH.name()) ||
            property.equals(StyleProperties.OUTLINE_OFFSET.name()) ||
            property.equals(StyleProperties.PADDING.name()) ||
            property.equals(StyleProperties.PADDING_TOP.name()) ||
            property.equals(StyleProperties.PADDING_RIGHT.name()) ||
            property.equals(StyleProperties.PADDING_BOTTOM.name()) ||
            property.equals(StyleProperties.PADDING_LEFT.name()) ||
            property.equals(StyleProperties.MARGIN.name()) ||
            property.equals(StyleProperties.MARGIN_TOP.name()) ||
            property.equals(StyleProperties.MARGIN_RIGHT.name()) ||
            property.equals(StyleProperties.MARGIN_BOTTOM.name()) ||
            property.equals(StyleProperties.MARGIN_LEFT.name()) ||
            property.equals(StyleProperties.TOP.name()) ||
            property.equals(StyleProperties.RIGHT.name()) ||
            property.equals(StyleProperties.BOTTOM.name()) ||
            property.equals(StyleProperties.LEFT.name()) ||
            property.equals(StyleProperties.GAP.name()) ||
            property.equals(StyleProperties.ROW_GAP.name()) ||
            property.equals(StyleProperties.COLUMN_GAP.name())) {
            return parseCssNumber(value);
        }

        // Opacity (0-1 range, unitless)
        if (property.equals(StyleProperties.OPACITY.name())) {
            return parseUnitlessFloat(value);
        }

        // Z-index (integer, no units)
        if (property.equals(StyleProperties.Z_INDEX.name())) {
            return parseInteger(value);
        }

        // Font weight (can be numeric 100-900 or keywords like 'normal', 'bold')
        if (property.equals(StyleProperties.FONT_WEIGHT.name())) {
            return parseFontWeight(value);
        }

        // Keywords and other properties stay as String
        // (display, position, overflow, text-align, font-family, cursor, etc.)
        return value;
    }

    /**
     * Parses a CSS number value, stripping common units like px, em, rem, %, etc.
     * For now, we just return the numeric value and ignore the unit.
     */
    private float parseCssNumber(String value) {
        if (value == null || value.isEmpty()) {
            return 0f;
        }

        // Strip common CSS units
        String numericPart = value.replaceAll("(px|em|rem|%|pt|vh|vw)$", "").trim();

        try {
            return Float.parseFloat(numericPart);
        } catch (NumberFormatException e) {
            LOG.warn("Failed to parse CSS number from value {0}.", value, e);
            return 0f;
        }
    }

    /**
     * Parses a unitless float value (e.g., opacity: 0.5)
     */
    private float parseUnitlessFloat(String value) {
        if (value == null || value.isEmpty()) {
            return 1.0f;
        }

        try {
            return Float.parseFloat(value.trim());
        } catch (NumberFormatException e) {
            LOG.warn("Failed to parse unitless float from value {0}.", value, e);
            return 1.0f;
        }
    }

    /**
     * Parses an integer value (e.g., z-index: 100)
     */
    private int parseInteger(String value) {
        if (value == null || value.isEmpty()) {
            return 0;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            LOG.warn("Failed to parse integer from value {0}.", value, e);
            return 0;
        }
    }

    /**
     * Parses font-weight which can be numeric (100-900) or keyword (normal, bold, etc.)
     */
    private int parseFontWeight(String value) {
        if (value == null || value.isEmpty()) {
            return 400; // normal
        }

        // Handle keyword values
        return switch (value.toLowerCase().trim()) {
            case "normal" -> 400;
            case "bold" -> 700;
            default -> {
                // Try to parse as numeric value (100, 200, ... 900)
                try {
                    int weight = Integer.parseInt(value.trim());
                    // Clamp to valid range
                    yield Math.max(100, Math.min(900, weight));
                } catch (NumberFormatException e) {
                    LOG.warn("Failed to parse font-weight from value {0}, using normal (400).", value, e);
                    yield 400;
                }
            }
        };
    }

    /**
     * Checks if a CSS selector (e.g., "panel.dark button") matches
     * a specific widget by checking its properties and ancestry.
     */
    private boolean matches(String selector, Styleable widget) {
        // Split selector by space: "panel.dark button" -> ["panel.dark", "button"]
        // Use regex to handle multiple consecutive spaces
        String[] parts = selector.trim().split("\\s+");

        Styleable currentWidget = widget;
        int partIndex = parts.length - 1; // Start with the right-most part

        while (currentWidget != null && partIndex >= 0) {
            String part = parts[partIndex];

            if (partMatches(part, currentWidget)) {
                // Good, this part matches. Move to the next part on the left.
                partIndex--;
            }

            // Always move up the tree to check the ancestor.
            // This is what makes "panel button" work.
            currentWidget = currentWidget.getStyleableParent();
        }

        // If we matched all parts, partIndex will be -1
        return (partIndex < 0);
    }

    /**
     * Helper to check if a single selector part (e.g., "button", "#id", ".class", "button:hover")
     * matches a single widget. This handles complex selectors like "button.primary:hover".
     */
    private static boolean partMatches(String part, Styleable widget) {
        // Parse the selector part into its components
        // Examples: "button:hover" -> type="button", pseudo="hover"
        //          "panel.dark" -> type="panel", class="dark"
        //          "#myId.primary:hover" -> id="myId", class="primary", pseudo="hover"

        String type = null;
        String id = null;
        List<String> classes = new ArrayList<>();
        List<String> pseudoClasses = new ArrayList<>();

        // Parse the selector part character by character
        StringBuilder current = new StringBuilder();
        char mode = 't'; // t=type, #=id, .=class, :=pseudo

        for (int i = 0; i < part.length(); i++) {
            char c = part.charAt(i);

            if (c == '#' || c == '.' || c == ':') {
                // Save the current token
                if (!current.isEmpty()) {
                    switch (mode) {
                        case 't' -> type = current.toString();
                        case '#' -> id = current.toString();
                        case '.' -> classes.add(current.toString());
                        case ':' -> pseudoClasses.add(current.toString());
                    }
                    current.setLength(0);
                }
                mode = c;
            } else {
                current.append(c);
            }
        }

        // Save the last token
        if (!current.isEmpty()) {
            switch (mode) {
                case 't' -> type = current.toString();
                case '#' -> id = current.toString();
                case '.' -> classes.add(current.toString());
                case ':' -> pseudoClasses.add(current.toString());
            }
        }

        // Now check if all conditions match
        if (type != null && !type.equals(widget.getStyleType())) {
            return false;
        }

        if (id != null && !id.equals(widget.getStyleId())) {
            return false;
        }

        for (String cls : classes) {
            if (!widget.getClasses().contains(cls)) {
                return false;
            }
        }

        for (String pseudo : pseudoClasses) {
            if (!widget.getPseudoClasses().contains(pseudo)) {
                return false;
            }
        }

        return true;
    }

}
