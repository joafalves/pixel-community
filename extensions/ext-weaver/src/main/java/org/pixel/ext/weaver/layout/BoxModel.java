package org.pixel.ext.weaver.layout;

import lombok.Getter;
import org.pixel.math.Rectangle;

@Getter
public class BoxModel {

    /*
     * The box model consists of four rectangles:
     * - Content Box: The innermost rectangle representing the content area.
     * - Padding Box: The content box plus padding.
     * - Border Box: The padding box plus border.
     * - Margin Box: The border box plus margin.
     *
     * +-----------------------------+
     * |         Margin Box          |
     * |  +-----------------------+  |
     * |  |      Border Box       |  |
     * |  |  +-----------------+  |  |
     * |  |  |   Padding Box   |  |  |
     * |  |  |  +-----------+  |  |  |
     * |  |  |  |  Content  |  |  |  |
     * |  |  |  |    Box    |  |  |  |
     * |  |  |  +-----------+  |  |  |
     * |  |  +-----------------+  |  |
     * |  +-----------------------+  |
     * +-----------------------------+
     */

    private final Rectangle contentBox = new Rectangle(0); // content area
    private final Rectangle paddingBox = new Rectangle(0); // content + padding
    private final Rectangle borderBox = new Rectangle(0);  // content + padding + border
    private final Rectangle marginBox = new Rectangle(0);  // content + padding + border + margin
}
