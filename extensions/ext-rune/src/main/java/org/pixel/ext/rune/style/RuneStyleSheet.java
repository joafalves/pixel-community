package org.pixel.ext.rune.style;

import org.pixel.ext.rune.widget.RuneWidget;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * CSS-inspired stylesheet with type, class, and ID selectors.
 * Computes final widget styles using cascade and specificity rules.
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
     * Compute the final style for a widget using CSS cascade rules.
     * Specificity order (lowest to highest):
     * 1. Type styles (automatic)
     * 2. Class styles (in order added)
     * 3. ID styles
     * 4. Inline styles
     * 5. State styles (within each level)
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
        
        // 2. Apply class styles in order (medium priority)
        Set<String> classes = widget.getClasses();
        if (classes != null) {
            for (String className : classes) {
                RuneStyle classStyle = classStyles.get(className);
                if (classStyle != null) {
                    computed.merge(classStyle, activeStates);
                }
            }
        }
        
        // 3. Apply ID style (high priority)
        String id = widget.getId();
        if (id != null) {
            RuneStyle idStyle = idStyles.get(id);
            if (idStyle != null) {
                computed.merge(idStyle, activeStates);
            }
        }
        
        // 4. Apply inline style (highest priority)
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
