/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.gui.style;

import static org.pixel.gui.style.StyleUtils.STYLE_PATH_SEPARATOR;

import cz.vutbr.web.css.CSSException;
import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.CombinedSelector;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.RuleBlock;
import cz.vutbr.web.css.RuleSet;
import cz.vutbr.web.css.StyleSheet;
import cz.vutbr.web.css.TermFunction;
import cz.vutbr.web.css.TermOperator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.commons.util.IOUtils;
import org.pixel.gui.style.processor.property.BackgroundPropertyProcessor;
import org.pixel.gui.style.processor.property.BorderPropertyProcessor;
import org.pixel.gui.style.processor.property.BoxShadowPropertyProcessor;
import org.pixel.gui.style.processor.property.BoxSizingPropertyProcessor;
import org.pixel.gui.style.processor.property.ColorPropertyProcessor;
import org.pixel.gui.style.processor.property.CursorPropertyProcessor;
import org.pixel.gui.style.processor.property.DisplayPropertyProcessor;
import org.pixel.gui.style.processor.property.FlexPropertyProcessor;
import org.pixel.gui.style.processor.property.FontPropertyProcessor;
import org.pixel.gui.style.processor.property.MarginPropertyProcessor;
import org.pixel.gui.style.processor.property.OriginPropertyProcessor;
import org.pixel.gui.style.processor.property.PaddingPropertyProcessor;
import org.pixel.gui.style.processor.property.PositionPropertyProcessor;
import org.pixel.gui.style.processor.property.SizePropertyProcessor;
import org.pixel.gui.style.processor.property.StylePropertyProcessor;
import org.pixel.gui.style.processor.property.TextPropertyProcessor;

public class StyleFactory {

    private static final Logger LOG = LoggerFactory.getLogger(StyleFactory.class);
    private static final ArrayList<StylePropertyProcessor> stylePropertyProcessors;

    static {
        stylePropertyProcessors = new ArrayList<>();
        stylePropertyProcessors.add(new BackgroundPropertyProcessor());
        stylePropertyProcessors.add(new BorderPropertyProcessor());
        stylePropertyProcessors.add(new ColorPropertyProcessor());
        stylePropertyProcessors.add(new FontPropertyProcessor());
        stylePropertyProcessors.add(new SizePropertyProcessor());
        stylePropertyProcessors.add(new TextPropertyProcessor());
        stylePropertyProcessors.add(new MarginPropertyProcessor());
        stylePropertyProcessors.add(new PaddingPropertyProcessor());
        stylePropertyProcessors.add(new PositionPropertyProcessor());
        stylePropertyProcessors.add(new OriginPropertyProcessor());
        stylePropertyProcessors.add(new DisplayPropertyProcessor());
        stylePropertyProcessors.add(new FlexPropertyProcessor());
        stylePropertyProcessors.add(new BoxShadowPropertyProcessor());
        stylePropertyProcessors.add(new CursorPropertyProcessor());
        stylePropertyProcessors.add(new BoxSizingPropertyProcessor());
    }

    /**
     * Get the computed properties from a value string (css-like)
     *
     * @param value (example: 'color: #333; background-color: red; ...')d
     * @return
     */
    public static List<StyleProperty> getProperties(String value) {
        List<StyleProperty> properties = new ArrayList<>();
        value = "mock {" + value + "}";

        StyleSheet css;
        try {
            css = CSSFactory.parseString(value, null);

        } catch (IOException | CSSException e) {
            LOG.error("Could not parse CSS style '{}'.", value);
            return null;
        }

        if (css.size() == 0 || !(css.get(0) instanceof RuleSet)) {
            return null;
        }

        RuleSet ruleSet = (RuleSet) css.get(0);
        for (Declaration declaration : ruleSet) {
            StyleProperty property = getStyleProperty(declaration);
            if (property != null) {
                properties.add(property);
            }
        }

        if (properties.size() > 0) {
            // merge similar styles into a single property object
            StyleUtils.mergeDuplicatedStyleProperties(properties);
        }

        return properties;
    }

    /**
     * @param filepath
     * @return
     */
    public static Style getStyle(String filepath) {
        String styleSrc = IOUtils.loadFileString(filepath);
        if (styleSrc == null) {
            LOG.warn("Could not load style from path '{}'.", filepath);
            return null;
        }

        StyleSheet css;
        try {
            css = CSSFactory.parseString(styleSrc, null);

        } catch (IOException | CSSException e) {
            LOG.error("Could not parse CSS style from path '{}'.", filepath);
            return null;
        }

        return new Style.StyleBuilder()
                .styleSelectors(getSelectors(css))
                .build();
    }

    /**
     * @param css
     * @return
     */
    private static List<StyleSelector> getSelectors(StyleSheet css) {
        ArrayList<StyleSelector> selectors = new ArrayList<>();

        for (RuleBlock<?> rule : css) {
            if (rule instanceof RuleSet) {
                RuleSet ruleSet = (RuleSet) rule;
                ArrayList<StyleProperty> properties = new ArrayList<>();
                for (Declaration declaration : ruleSet) {
                    StyleProperty property = getStyleProperty(declaration);
                    if (property != null) {
                        properties.add(property);
                    }
                }

                if (properties.size() > 0 && ((RuleSet) rule).getSelectors().length > 0) {
                    // merge similar styles into a single property object
                    StyleUtils.mergeDuplicatedStyleProperties(properties);

                    // for each selector, we must add out our structure in the correct path:
                    for (CombinedSelector rawSelector : ((RuleSet) rule).getSelectors()) {
                        StyleSelector selector = StyleSelector.builder()
                                .name(rawSelector.getLastSelector().toString().trim())
                                .properties(properties)
                                .children(new ArrayList<>())
                                .build();
                        addSelector(selectors, rawSelector.toString().trim(), selector);
                    }
                }
            }
        }

        return selectors;
    }

    /**
     * @param selectors
     * @param path
     * @param selector
     */
    private static void addSelector(List<StyleSelector> selectors, String path, StyleSelector selector) {
        if (!path.contains(STYLE_PATH_SEPARATOR)) {
            // this is the selector to add..
            StyleSelector levelSelector = getSelectorByName(selectors, path);
            if (levelSelector != null) {
                // already exists... add properties to the current selector and merge
                levelSelector.getProperties().addAll(selector.getProperties());
                StyleUtils.mergeDuplicatedStyleProperties(levelSelector.getProperties());

            } else {
                // doesn't exist yet:
                selectors.add(selector);
            }

        } else {
            // get the current level selector (based on path)
            String[] pathSplit = path.split(STYLE_PATH_SEPARATOR, 2);
            String levelSelectorName = pathSplit[0];
            StyleSelector levelSelector = getSelectorByName(selectors, levelSelectorName);

            // selector already exists?
            if (levelSelector == null) {
                // create empty style selector to fill the need:
                levelSelector = StyleSelector.builder()
                        .name(levelSelectorName)
                        .properties(new ArrayList<>())
                        .children(new ArrayList<>())
                        .build();
                selectors.add(levelSelector);
            }

            addSelector(levelSelector.getChildren(), pathSplit[1], selector);
        }
    }

    /**
     * @param selectors
     * @param name
     * @return
     */
    private static StyleSelector getSelectorByName(List<StyleSelector> selectors, String name) {
        for (StyleSelector selector : selectors) {
            if (selector.getName().equals(name)) {
                return selector;
            }
        }

        return null;
    }

    /**
     * @param declaration
     * @return
     */
    private static StyleProperty getStyleProperty(Declaration declaration) {
        StylePropertyProcessor processor = getStyleProcessor(declaration.getProperty());

        if (processor != null) {
            ArrayList<StylePropertyValue> values = new ArrayList<>();
            // process each property inside the selector
            declaration.forEach(term -> {
                if (term instanceof TermFunction) {
                    // this is a function...
                    ArrayList<String> parameters = new ArrayList<>();
                    for (Object parameter : ((List) term.getValue())) {
                        if (!(parameter instanceof TermOperator)) {
                            parameters.add(parameter.toString());
                        }
                    }

                    values.add(StylePropertyValueFunction.functionBuilder()
                            .functionName(((TermFunction) term).getFunctionName())
                            .parameters(parameters)
                            .value(term.toString().trim())
                            .separator(term.getOperator() != null ? term.getOperator().value() : "") // TODO: check this
                            .build());

                } else {
                    values.add(StylePropertyValue.builder()
                            .value(term.toString().trim())
                            .separator(term.getOperator() != null ? term.getOperator().value() : "") // TODO: check this
                            .build());
                }
            });

            return processor.process(declaration.getProperty(), values);
        }

        return null;
    }

    /**
     * @param selectorName
     * @return
     */
    private static StylePropertyProcessor getStyleProcessor(String selectorName) {
        for (StylePropertyProcessor stylePropertyProcessor : stylePropertyProcessors) {
            if (stylePropertyProcessor.match(selectorName)) {
                return stylePropertyProcessor;
            }
        }

        return null;
    }

}
