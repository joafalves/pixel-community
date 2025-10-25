package org.pixel.ext.weaver.style;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Defines the contract for a UI component that can be styled.
 * This interface provides the StyleEngine with all the information
 * needed to match CSS selectors against a component's identity,
 * state, and position in the UI tree.
 */
public interface Styleable {

    /**
     * Gets the CSS "type" or "element" selector.
     * @return e.g., "button", "panel", "label"
     */
    String getStyleType();

    /**
     * Gets the CSS "#id" selector.
     * This should be unique.
     * @return e.g., "main-menu", "confirm-button", or null if not set
     */
    String getStyleId();

    /**
     * Gets the current style revision number.
     * This should be incremented whenever the style-related
     * state of the component changes (e.g., classes or pseudo-classes).
     * @return An integer revision number
     */
    int getStyleVersion();

    /**
     * Gets the set of CSS ".class" selectors.
     * @return A Set of strings, e.g., ["dark-mode", "rounded", "primary"]
     */
    Set<String> getClasses();

    /**
     * Gets the set of active CSS ":pseudo-class" selectors.
     * This set should be updated dynamically by the widget's logic.
     * @return A Set of strings, e.g., ["hover", "active"] or an empty set
     */
    Set<String> getPseudoClasses();

    /**
     * Gets the inline styles directly applied to this component.
     * These styles have the highest specificity.
     * @return A Map of property names to their values
     */
    Map<String, Object> getInlineStyleMap();

    /**
     * Gets the direct parent of this component in the hierarchy.
     * This is crucial for matching descendant selectors.
     * @return The parent as a Styleable, or null if this is the root.
     */
    Styleable getStyleableParent();

    /**
     * Gets the direct children of this component.
     * This is needed for invalidating child caches on inheritance changes.
     * @return A List of Styleable children.
    List<Styleable> getStyleableChildren(); */
}