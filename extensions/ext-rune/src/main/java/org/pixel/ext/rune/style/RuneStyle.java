package org.pixel.ext.rune.style;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Container for style properties with state support.
 * Represents computed or declared styles for a widget.
 */
@Getter
public class RuneStyle {
    // Base properties (always active)
    private final Map<StyleProperty<?>, Object> properties = new HashMap<>();
    
    // State-specific properties (hover, active, focused, disabled, etc.)
    private final Map<String, Map<StyleProperty<?>, Object>> stateProperties = new HashMap<>();

    /**
     * Set a property value.
     * Automatically expands shorthand properties (e.g., PADDING → PADDING_TOP/RIGHT/BOTTOM/LEFT).
     *
     * @param property The property to set
     * @param value The value to set
     * @return This style for chaining
     */
    public <T> RuneStyle set(StyleProperty<T> property, T value) {
        properties.put(property, value);
        expandShorthandProperty(properties, property, value);
        return this;
    }
    
    /**
     * Expand shorthand properties (PADDING, MARGIN) to individual properties.
     * 
     * @param targetMap The map to add expanded properties to
     * @param property The property being set
     * @param value The value being set
     */
    private void expandShorthandProperty(Map<StyleProperty<?>, Object> targetMap, StyleProperty<?> property, Object value) {
        if (property == StyleProperties.PADDING && value instanceof Float padding) {
            targetMap.put(StyleProperties.PADDING_TOP, padding);
            targetMap.put(StyleProperties.PADDING_RIGHT, padding);
            targetMap.put(StyleProperties.PADDING_BOTTOM, padding);
            targetMap.put(StyleProperties.PADDING_LEFT, padding);
        } else if (property == StyleProperties.MARGIN && value instanceof Float margin) {
            targetMap.put(StyleProperties.MARGIN_TOP, margin);
            targetMap.put(StyleProperties.MARGIN_RIGHT, margin);
            targetMap.put(StyleProperties.MARGIN_BOTTOM, margin);
            targetMap.put(StyleProperties.MARGIN_LEFT, margin);
        }
    }

    /**
     * Get a property value (type-safe).
     *
     * @param property The property to get
     * @return The value, or the property's default if not set
     */
    @SuppressWarnings("unchecked")
    public <T> T get(StyleProperty<T> property) {
        Object value = properties.get(property);
        if (value != null) {
            return (T) value;
        }
        return property.getDefaultValue();
    }

    /**
     * Check if a property is set.
     *
     * @param property The property to check
     * @return True if the property has been set
     */
    public boolean has(StyleProperty<?> property) {
        return properties.containsKey(property);
    }

    /**
     * Set a property value for a specific state/pseudo-class.
     * Supports CSS-like pseudo-classes: ":hover", ":focused", ":pressed", ":disabled"
     *
     * @param pseudoClass The pseudo-class selector (e.g., ":hover", ":focused:hover")
     * @param property The property to set
     * @param value The value to set
     * @return This style for chaining
     */
    public <T> RuneStyle set(String pseudoClass, StyleProperty<T> property, T value) {
        if (pseudoClass == null || pseudoClass.isEmpty()) {
            return set(property, value);  // Base state
        }
        
        Map<StyleProperty<?>, Object> stateProps = stateProperties.computeIfAbsent(pseudoClass, k -> new HashMap<>());
        stateProps.put(property, value);
        expandShorthandProperty(stateProps, property, value);
        
        return this;
    }

    /**
     * Define state-specific properties.
     *
     * @param stateName The state name (e.g., "hover", "active", "focused")
     * @param stateStyle The style to apply in this state
     * @return This style for chaining
     */
    public RuneStyle state(String stateName, RuneStyle stateStyle) {
        stateProperties.put(stateName, new HashMap<>(stateStyle.properties));
        return this;
    }

    /**
     * Get a property value considering active states.
     * State properties override base properties.
     *
     * @param property The property to get
     * @param activeStates Currently active states
     * @return The value considering states, or default if not set
     */
    @SuppressWarnings("unchecked")
    public <T> T get(StyleProperty<T> property, Set<String> activeStates) {
        // Check states in reverse order (later states override earlier ones)
        if (activeStates != null) {
            for (String state : activeStates) {
                Map<StyleProperty<?>, Object> stateProps = stateProperties.get(state);
                if (stateProps != null && stateProps.containsKey(property)) {
                    return (T) stateProps.get(property);
                }
            }
        }
        
        // Fall back to base property
        return get(property);
    }

    /**
     * Merge another style into this one.
     * Properties from the other style override properties in this one.
     *
     * @param other The style to merge
     * @param activeStates Currently active states to consider
     */
    public void merge(RuneStyle other, Set<String> activeStates) {
        if (other == null) return;
        
        // Merge base properties
        properties.putAll(other.properties);
        
        // Merge state properties
        if (activeStates != null) {
            for (String state : activeStates) {
                Map<StyleProperty<?>, Object> otherStateProps = other.stateProperties.get(state);
                if (otherStateProps != null) {
                    properties.putAll(otherStateProps);
                }
            }
        }
    }

    /**
     * Create a copy of this style.
     *
     * @return A new RuneStyle with the same properties
     */
    public RuneStyle copy() {
        RuneStyle copy = new RuneStyle();
        copy.properties.putAll(this.properties);
        
        for (Map.Entry<String, Map<StyleProperty<?>, Object>> entry : stateProperties.entrySet()) {
            copy.stateProperties.put(entry.getKey(), new HashMap<>(entry.getValue()));
        }
        
        return copy;
    }
}
