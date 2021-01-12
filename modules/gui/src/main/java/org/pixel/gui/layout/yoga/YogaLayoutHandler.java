/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.gui.layout.yoga;

import org.lwjgl.util.yoga.YGLayout;
import org.lwjgl.util.yoga.YGNode;
import org.lwjgl.util.yoga.Yoga;
import org.pixel.gui.component.UIComponent;
import org.pixel.gui.component.UIContainer;
import org.pixel.gui.layout.LayoutHandler;
import org.pixel.gui.style.StyleUtils;
import org.pixel.gui.style.model.DisplayUnit;
import org.pixel.gui.style.model.FlexDirection;
import org.pixel.gui.style.model.FlexWrap;
import org.pixel.gui.style.properties.FlexStyle;
import org.pixel.gui.style.properties.MarginStyle;
import org.pixel.gui.style.properties.PaddingStyle;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.util.yoga.Yoga.*;

public class YogaLayoutHandler implements LayoutHandler {

    private final Long root;
    private Map<UIComponent, Long> nodeMap;
    private UIContainer container;

    private FlexStyle flexStyle;
    private PaddingStyle paddingStyle;

    /**
     * Constructor
     */
    public YogaLayoutHandler(UIContainer container) {
        this.root = Yoga.YGNodeNew();
        this.nodeMap = new HashMap<>();
        this.container = container;
    }

    @Override
    public void reload() {
        flexStyle = StyleUtils.getStyleProperty(container, FlexStyle.class);
        paddingStyle = StyleUtils.getStyleProperty(container, PaddingStyle.class);

        YGNodeStyleSetFlexDirection(root, convert(flexStyle.getDirection()));
        YGNodeStyleSetFlexWrap(root, convert(flexStyle.getWrap()));

        // set the margin of the child elements based on the padding style:
        YGNodeStyleSetMargin(root, YGEdgeLeft, paddingStyle.getLeft());
        YGNodeStyleSetMargin(root, YGEdgeRight, paddingStyle.getRight());
        YGNodeStyleSetMargin(root, YGEdgeTop, paddingStyle.getTop());
        YGNodeStyleSetMargin(root, YGEdgeBottom, paddingStyle.getBottom());
    }

    @Override
    public void computeLayout() {
        if (nodeMap.size() == 0) {
            return; // nothing to do...
        }

        nodeMap.forEach((component, node) -> {
            // get or create (default) flex style for each child node:
            FlexStyle nodeFlexStyle = StyleUtils.getStyleProperty(component, FlexStyle.class);
            MarginStyle nodeMarginStyle = StyleUtils.getStyleProperty(component, MarginStyle.class);

            applyFlexBasis(nodeFlexStyle, node);
            YGNodeStyleSetFlexGrow(node, nodeFlexStyle.getGrow().getValue());
            YGNodeStyleSetFlexShrink(node, nodeFlexStyle.getShrink().getValue());
            YGNodeStyleSetPositionType(node, YGPositionTypeRelative);
            YGNodeStyleSetWidth(node, component.getVirtualWidth());
            YGNodeStyleSetHeight(node, component.getVirtualHeight());
            YGNodeStyleSetMargin(node, YGEdgeLeft, nodeMarginStyle.getLeft());
            YGNodeStyleSetMargin(node, YGEdgeRight, nodeMarginStyle.getRight());
            YGNodeStyleSetMargin(node, YGEdgeTop, nodeMarginStyle.getTop());
            YGNodeStyleSetMargin(node, YGEdgeBottom, nodeMarginStyle.getBottom());
        });

        YGNodeCalculateLayout(root, container.getVirtualWidth(), container.getVirtualHeight(), YGFlexDirectionColumn);

        nodeMap.forEach((component, node) -> {
            YGLayout layout = YGNode.create(node).layout();
            component.setBounds(
                    layout.positions(YGEdgeLeft),
                    layout.positions(YGEdgeTop),
                    layout.dimensions(YGDimensionWidth),
                    layout.dimensions(YGDimensionHeight)
            );
        });
    }

    @Override
    public void componentAdded(UIComponent component) {
        if (!nodeMap.containsKey(component)) {
            // create node and insert on the root:
            long node = Yoga.YGNodeNew();
            Yoga.YGNodeInsertChild(root, node, nodeMap.size());

            // save the node on the internal node map:
            nodeMap.put(component, node);

            if (flexStyle != null) {
                computeLayout();
            }
        }
    }

    @Override
    public void componentRemoved(UIComponent component) {
        Long node = nodeMap.get(component);
        if (node != null) {
            Yoga.YGNodeRemoveChild(root, node);
            nodeMap.remove(component);
            computeLayout();
        }
    }

    /**
     * Dispose function
     */
    @Override
    public void dispose() {
        nodeMap.forEach((component, node) -> YGNodeFree(node));
        YGNodeFree(root);
    }

    private void applyFlexBasis(FlexStyle style, Long node) {
        if (flexStyle.getBasis().getType().equals(DisplayUnit.Type.AUTO)) {
            YGNodeStyleSetFlexBasisAuto(node);

        } else {
            YGNodeStyleSetFlexBasis(node, style.getBasis().getValue());
        }
    }

    private static int convert(FlexDirection flexDirection) {
        switch (flexDirection) {
            case COLUMN:
                return YGFlexDirectionColumn;
            case COLUMN_REVERSE:
                return YGFlexDirectionColumnReverse;
            case ROW_REVERSE:
                return YGFlexDirectionRowReverse;
            case ROW:
            default:
                return YGFlexDirectionRow;
        }
    }

    private static int convert(FlexWrap flexWrap) {
        switch (flexWrap) {
            case WRAP:
                return YGWrapWrap;

            case WRAP_REVERSE:
                return YGWrapReverse;

            case NO_WRAP:
            default:
                return YGWrapNoWrap;
        }
    }
}
