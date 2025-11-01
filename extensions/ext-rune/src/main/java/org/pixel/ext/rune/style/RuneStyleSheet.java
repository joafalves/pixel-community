package org.pixel.ext.rune.style;

import org.pixel.ext.rune.widget.RuneWidget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * CSS-inspired stylesheet with type, class, and ID selectors.
 * Computes final widget styles using cascade and specificity rules.
 *
 * <p>Supports descendant selectors:
 * <ul>
 *   <li>Type: "label" - matches all labels</li>
 *   <li>Class: ".panel-title" - matches widgets with panel-title class</li>
 *   <li>Descendant: ".panel-title label" - matches labels inside any ancestor with panel-title class</li>
 *   <li>Multiple: ".workspace .panel label" - matches labels inside panels inside workspace</li>
 * </ul>
 */
public class RuneStyleSheet {
    // Type-based styles (automatic based on widget type)
    // CSS equivalent: button { ... }
    private final Map<String, RuneStyle> typeStyles = new HashMap<>();

    // Class-based styles (manual via addClass())
    // CSS equivalent: .primary { ... }
    private final Map<String, RuneStyle> classStyles = new HashMap<>();

    // ID-based styles (unique widgets)
    // CSS equivalent: #submitButton { ... }
    private final Map<String, RuneStyle> idStyles = new HashMap<>();

    // Descendant selectors (context-based styling)
    // CSS equivalent: .panel-title label { ... }
    // Parsed rules for efficient matching
    private final List<DescendantRule> descendantRules = new ArrayList<>();

    /**
     * Internal class representing a parsed descendant selector rule.
     * Example: ".panel-title label" -> ancestors=[".panel-title"], target="label"
     */
    private static class DescendantRule {
        final List<String> ancestorSelectors;  // Selectors that ancestors must match (in order, outermost first)
        final String targetSelector;            // Selector that the target widget must match
        final RuneStyle style;                  // Style to apply when rule matches

        DescendantRule(List<String> ancestorSelectors, String targetSelector, RuneStyle style) {
            this.ancestorSelectors = ancestorSelectors;
            this.targetSelector = targetSelector;
            this.style = style;
        }

        /**
         * Check if this rule matches the given widget.
         * Widget must match targetSelector AND have ancestors matching ancestorSelectors.
         */
        boolean matches(RuneWidget widget) {
            // Check if widget matches the target selector
            if (!matchesSelector(widget, targetSelector)) {
                return false;
            }

            // Check if all ancestor selectors are satisfied
            // Walk up parent chain and match selectors in reverse order (innermost first)
            RuneWidget current = widget.getParent();
            for (int i = ancestorSelectors.size() - 1; i >= 0; i--) {
                String ancestorSelector = ancestorSelectors.get(i);
                boolean found = false;

                // Walk up until we find an ancestor matching this selector
                while (current != null) {
                    if (matchesSelector(current, ancestorSelector)) {
                        found = true;
                        current = current.getParent();  // Continue from this ancestor's parent
                        break;
                    }
                    current = current.getParent();
                }

                if (!found) {
                    return false;  // Required ancestor not found
                }
            }

            return true;  // All conditions satisfied
        }

        /**
         * Check if a widget matches a simple selector (type or class).
         */
        private static boolean matchesSelector(RuneWidget widget, String selector) {
            if (selector.startsWith(".")) {
                // Class selector: ".panel-title"
                String className = selector.substring(1);
                return widget.hasClass(className);
            } else {
                // Type selector: "label"
                return selector.equals(widget.getTypeName());
            }
        }
    }

    /**
     * Define a style for a widget TYPE.
     * All widgets of this type automatically get this style.
     * 
     * <p>If a type style already exists, properties are merged (new properties override existing).
     * This allows child themes to extend parent theme definitions without losing inherited properties.
     *
     * @param typeName The widget type name (e.g., "button", "label", "textfield")
     * @param builder Consumer to configure the style
     * @return This stylesheet for chaining
     */
    public RuneStyleSheet type(String typeName, Consumer<RuneStyle> builder) {
        // Get existing style or create new one
        RuneStyle style = typeStyles.computeIfAbsent(typeName, k -> new RuneStyle());
        builder.accept(style);
        return this;
    }

    /**
     * Define a style CLASS.
     * Widgets must explicitly add this class via addClass().
     * 
     * <p>If a style class already exists, properties are merged (new properties override existing).
     * This allows child themes to extend parent theme definitions without losing inherited properties.
     *
     * @param className The class name (e.g., "primary", "warning", "large")
     * @param builder Consumer to configure the style
     * @return This stylesheet for chaining
     */
    public RuneStyleSheet styleClass(String className, Consumer<RuneStyle> builder) {
        // Get existing style or create new one
        RuneStyle style = classStyles.computeIfAbsent(className, k -> new RuneStyle());
        builder.accept(style);
        return this;
    }

    /**
     * Define a style for a specific ID.
     * Only the widget with this ID will get this style.
     *
     * <p>If an ID style already exists, properties are merged (new properties override existing).
     * This allows child themes to extend parent theme definitions without losing inherited properties.
     *
     * @param idName The unique ID (e.g., "submitButton", "titleLabel")
     * @param builder Consumer to configure the style
     * @return This stylesheet for chaining
     */
    public RuneStyleSheet id(String idName, Consumer<RuneStyle> builder) {
        // Get existing style or create new one
        RuneStyle style = idStyles.computeIfAbsent(idName, k -> new RuneStyle());
        builder.accept(style);
        return this;
    }

    /**
     * Define a descendant selector style.
     * Matches widgets when they have specific ancestors in their parent chain.
     *
     * <p>Selector format: "ancestor descendant" or "ancestor1 ancestor2 descendant"
     * <ul>
     *   <li>".panel-title label" - labels inside any ancestor with class "panel-title"</li>
     *   <li>".workspace panel" - panels inside any ancestor with class "workspace"</li>
     *   <li>"panel label" - labels inside any ancestor of type "panel"</li>
     *   <li>".workspace .panel label" - labels inside panel inside workspace</li>
     * </ul>
     *
     * <p>Note: Ancestors can be at any level (not just direct parent).
     * Use a leading dot for class selectors, no dot for type selectors.
     *
     * @param selector The descendant selector (e.g., ".panel-title label")
     * @param builder Consumer to configure the style
     * @return This stylesheet for chaining
     */
    public RuneStyleSheet descendant(String selector, Consumer<RuneStyle> builder) {
        // Parse selector: split by whitespace
        String[] parts = selector.trim().split("\\s+");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Descendant selector must have at least 2 parts: " + selector);
        }

        // Last part is the target, everything before is ancestors
        List<String> ancestorSelectors = new ArrayList<>();
        for (int i = 0; i < parts.length - 1; i++) {
            ancestorSelectors.add(parts[i]);
        }
        String targetSelector = parts[parts.length - 1];

        // Create or find existing rule with same selectors
        DescendantRule rule = null;
        for (DescendantRule existing : descendantRules) {
            if (existing.ancestorSelectors.equals(ancestorSelectors) &&
                existing.targetSelector.equals(targetSelector)) {
                rule = existing;
                break;
            }
        }

        if (rule == null) {
            // Create new rule
            RuneStyle style = new RuneStyle();
            rule = new DescendantRule(ancestorSelectors, targetSelector, style);
            descendantRules.add(rule);
        }

        // Apply builder to rule's style
        builder.accept(rule.style);
        return this;
    }

    /**
     * Compute the final style for a widget using CSS cascade rules.
     * Specificity order (lowest to highest):
     * 1. Type styles (automatic)
     * 2. Descendant selector styles (context-based)
     * 3. Class styles (in order added)
     * 4. ID styles
     * 5. Inline styles
     * 6. State styles (within each level)
     *
     * @param widget The widget to compute styles for
     * @param activeStates Currently active states (e.g., "hover", "active")
     * @return The computed style
     */
    public RuneStyle computeStyle(RuneWidget widget, Set<String> activeStates) {
        RuneStyle computed = new RuneStyle();

        // 1. Apply type style (lowest priority)
        String typeName = widget.getTypeName();
        if (typeName != null) {
            // Apply base type style
            RuneStyle typeStyle = typeStyles.get(typeName);
            if (typeStyle != null) {
                computed.merge(typeStyle, activeStates);
            }

            // Apply pseudo-class type styles (e.g., "button:hover")
            if (activeStates != null) {
                for (String state : activeStates) {
                    String pseudoTypeName = typeName + state;  // e.g., "button:hover"
                    RuneStyle pseudoTypeStyle = typeStyles.get(pseudoTypeName);
                    if (pseudoTypeStyle != null) {
                        computed.merge(pseudoTypeStyle, null);  // Don't apply states again
                    }
                }
            }
        }

        // 2. Apply descendant selector styles (context-based)
        // These have higher priority than base type styles but lower than class styles
        for (DescendantRule rule : descendantRules) {
            if (rule.matches(widget)) {
                computed.merge(rule.style, activeStates);
            }
        }

        // 3. Apply class styles in order (medium priority)
        Set<String> classes = widget.getClasses();
        if (classes != null) {
            for (String className : classes) {
                RuneStyle classStyle = classStyles.get(className);
                if (classStyle != null) {
                    computed.merge(classStyle, activeStates);
                }
            }
        }

        // 4. Apply ID style (high priority)
        String id = widget.getId();
        if (id != null) {
            RuneStyle idStyle = idStyles.get(id);
            if (idStyle != null) {
                computed.merge(idStyle, activeStates);
            }
        }

        // 5. Apply inline style (highest priority)
        RuneStyle inlineStyle = widget.getInlineStyle();
        if (inlineStyle != null) {
            computed.merge(inlineStyle, activeStates);
        }

        return computed;
    }

    /**
     * Get a type style directly (for inspection/debugging).
     *
     * @param typeName The type name
     * @return The style or null if not defined
     */
    public RuneStyle getTypeStyle(String typeName) {
        return typeStyles.get(typeName);
    }

    /**
     * Get a class style directly (for inspection/debugging).
     *
     * @param className The class name
     * @return The style or null if not defined
     */
    public RuneStyle getClassStyle(String className) {
        return classStyles.get(className);
    }

    /**
     * Get an ID style directly (for inspection/debugging).
     *
     * @param idName The ID
     * @return The style or null if not defined
     */
    public RuneStyle getIdStyle(String idName) {
        return idStyles.get(idName);
    }
}
