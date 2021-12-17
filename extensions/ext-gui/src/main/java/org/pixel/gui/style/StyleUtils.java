/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.gui.style;

import lombok.ToString;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.gui.component.UIComponent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@ToString
public class StyleUtils {

    private static final Logger LOG = LoggerFactory.getLogger(StyleUtils.class);

    public static final String STYLE_PATH_SEPARATOR = " ";
    public static final String STYLE_CLASS_SEPARATOR = ".";

    /**
     * Merge duplicated style properties
     *
     * @param properties
     */
    public static void mergeDuplicatedStyleProperties(List<StyleProperty> properties) {
        for (int i = 0, j = 1; i < properties.size() - 1; ++j) {
            if (properties.get(i).getClass().equals(properties.get(j).getClass())) {
                // they have the same class, merge properties and prefer the highest priority one
                properties.get(i).merge(properties.get(j));
                properties.remove(j);
                --j;

            } else if (properties.get(i).getName().equals(properties.get(j).getName())) {
                // they have the same name but different implementations, just remove the one with least priority
                properties.remove(i);
                j = i;
            }

            if (j >= properties.size() - 1) {
                i++;
                j = i;
            }
        }
    }

    /**
     * Get requested style property object. If the style doesn't exist on the given properties, a new StyleProperty of
     * the given type is created (default values applied)
     *
     * @param component
     * @param type
     * @return
     */
    public static <T> T getStyleProperty(UIComponent component, Class<T> type) {
        return getStyleProperty(component, type, true);
    }

    /**
     * Get requested style property object
     *
     * @param component
     * @param type
     * @param defaultOnMissing If true and the style doesn't exist on the given properties, a new StyleProperty of
     *                         the given type is created (default values applied)
     * @return
     */
    public static <T> T getStyleProperty(UIComponent component, Class<T> type, boolean defaultOnMissing) {
        List<StyleProperty> properties = component.getStyleProperties();
        if (properties == null) {
            return null;
        }

        for (StyleProperty property : properties) {
            if (type.isInstance(property)) {
                property.setUnassignedProperties(); // ensure all properties are assigned
                return (T) property;
            }
        }

        if (defaultOnMissing && StyleProperty.class.isAssignableFrom(type) && !(Modifier.isAbstract(type.getModifiers()))) {
            try {
                T instance = type.getDeclaredConstructor().newInstance();
                ((StyleProperty) instance).setUnassignedProperties();

                // cache instance
                properties.add((StyleProperty) instance);

                return instance;

            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                LOG.error("Exception caught!", e);
            }
        }

        return null;
    }

    /**
     * @param component
     * @param style
     * @return
     */
    public static List<StyleProperty> getComputedProperties(UIComponent component, Style style) {
        String target, path;
        String stylePath = component.getComputedStylePath();
        if (!stylePath.contains(STYLE_PATH_SEPARATOR)) {
            path = "";
            target = stylePath;

        } else {
            String[] split = stylePath.split(STYLE_PATH_SEPARATOR);
            path = String.join(STYLE_PATH_SEPARATOR, Arrays.copyOf(split, split.length - 1));
            target = split[split.length - 1];
        }

        List<StyleProperty> properties = new ArrayList<>();
        List<StyleComputeData> styleComputeDataList = new ArrayList<>();
        List<String> targetParts = splitNodeParts(target);
        addComputedProperties(component, style.getStyleSelectors(), styleComputeDataList, path, targetParts, 0);

        // style computed properties:
        if (styleComputeDataList.size() > 0) {
            // sort by level (higher levels will have higher priority - must added last)
            styleComputeDataList.sort(Comparator.comparingInt(StyleComputeData::getLevel));

            for (StyleComputeData styleComputeData : styleComputeDataList) {
                properties.addAll(styleComputeData.getProperties());
            }

            mergeDuplicatedStyleProperties(properties);
        }

        // check component direct properties:
        if (component.getDirectProperties() != null) {
            // insert at position 0 as they have priority
            properties.addAll(component.getDirectProperties());
            mergeDuplicatedStyleProperties(properties);
        }

        return properties;
    }

    /**
     * @param value
     * @return
     */
    public static Float stripUnitValue(String value) {
        value = value.trim().replaceAll("px|em|%", "").replaceAll("initial|none|auto", "0");
        if (!value.isEmpty()) {
            return Float.parseFloat(value);
        }

        return null;
    }

    /**
     * @param node
     * @return
     */
    private static List<String> splitNodeParts(String node) {
        // OK, so the goal here is to make sure that all possibilities can be matched on the style tree.
        // for ex. given the style path of "panel:hover.myClass" shall result in the following result:
        // panel, panel:hover, .myClass, myClass:hover, panel:hover.myClass, panel.myClass...
        List<String> parts = new ArrayList<>();
        String[] arr = node.split("\\.");
        String suffix = null;
        int idx = arr[0].indexOf(":");
        if (idx > 0) {
            suffix = arr[0].substring(idx);
        }

        for (int i = 0; i < arr.length; ++i) {
            if (i == 0) {
                if (suffix != null) {
                    parts.add(arr[0].replace(suffix, ""));
                }
                parts.add(arr[0]);

            } else {
                parts.add(STYLE_CLASS_SEPARATOR + arr[i]);
                if (suffix != null) {
                    parts.add(STYLE_CLASS_SEPARATOR + arr[i] + suffix);

                    parts.add(arr[0].replace(suffix, "") + STYLE_CLASS_SEPARATOR + arr[i]);
                    parts.add(arr[0] + STYLE_CLASS_SEPARATOR + arr[i]);
                    parts.add(arr[0] + STYLE_CLASS_SEPARATOR + arr[i] + suffix);

                } else {
                    parts.add(arr[0] + STYLE_CLASS_SEPARATOR + arr[i]);
                }
            }
        }

        return parts;
    }

    /**
     * @param component
     * @param selectors
     * @param out
     * @param path
     * @param targetParts
     * @param level
     * @return
     */
    private static void addComputedProperties(UIComponent component, List<StyleSelector> selectors,
                                              List<StyleComputeData> out, String path, List<String> targetParts, int level) {
        // check target with selectors
        for (String part : targetParts) {
            addCompatibleSelectorProperties(selectors, out, part, level);
        }

        if (!path.trim().isEmpty()) {
            String[] split = path.split(STYLE_PATH_SEPARATOR);
            List<StyleSelector> compatibleSelectors = new ArrayList<>();
            for (String part : splitNodeParts(split[0])) {
                addCompatibleSelectors(selectors, compatibleSelectors, part);
            }

            if (compatibleSelectors.size() > 0) {
                // split current path
                path = split.length > 1 ? path.substring(split[0].length() + 1) : "";
                // try to gather more properties from plausible selectors
                for (StyleSelector compatibleSelector : compatibleSelectors) {
                    addComputedProperties(component, compatibleSelector.getChildren(), out, path, targetParts, level + 1);
                }
            }
        }
    }

    /**
     * @param selectors
     * @param eval
     * @return
     */
    private static void addCompatibleSelectors(List<StyleSelector> selectors, List<StyleSelector> out, String eval) {
        for (StyleSelector selector : selectors) {
            if (selector.getName().equals(eval) && selector.getChildrenCount() > 0) { // we only want paths here..
                if (!out.contains(selector)) {
                    out.add(selector);
                }
            }
        }
    }

    /**
     * @param selectors
     * @param styleComputeData
     * @param eval
     */
    private static void addCompatibleSelectorProperties(List<StyleSelector> selectors, List<StyleComputeData> styleComputeData,
                                                        String eval, int level) {
        selectors.forEach(styleSelector -> {
            if (styleSelector.getName().equals(eval)) {
                styleComputeData.add(StyleComputeData.builder()
                        .level(level)
                        .properties(cloneProperties(styleSelector.getProperties()))
                        .build());
            }
        });
    }

    /**
     * @param properties
     * @return
     */
    private static List<StyleProperty> cloneProperties(List<StyleProperty> properties) {
        ArrayList<StyleProperty> clone = new ArrayList<>();
        properties.forEach(styleProperty -> clone.add(styleProperty.clone()));

        return clone;
    }

}
