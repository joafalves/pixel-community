# Canvas Renderer - Text Quality Improvements

**Date:** October 15, 2025  
**Issues Fixed:** Jagged text & Missing shadows

---

## Problem 1: Jagged Text (Aliasing)

### Root Cause
The text appeared jagged because we were using `stbtt_GetGlyphBitmap()` which generates hard-edged, non-antialiased bitmaps.

### Solutions Applied

1. **Switched to Subpixel Rendering**
   - Changed from `stbtt_GetGlyphBitmap()` to `stbtt_GetGlyphBitmapSubpixel()`
   - This provides better quality with fractional pixel positioning
   - File: `GlSdfFontGenerator.java` (line ~125)

2. **Added Mipmap Generation**
   - Enabled `GL_LINEAR_MIPMAP_LINEAR` for minification filter
   - Generates mipmaps after texture upload with `glGenerateMipmap()`
   - This provides smoother text at different scales
   - File: `GlSdfFontGenerator.java` (line ~190-206)

3. **Adaptive Smoothness**
   - Changed from fixed `0.05f` smoothness to adaptive calculation
   - Formula: `smoothness = max(0.02, min(0.15, 3.0 / fontSize))`
   - Smaller fonts get more smoothing, larger fonts stay sharp
   - File: `GlSdfTextRenderer.java` (line ~145)

---

## Problem 2: Missing Drop Shadows

### Root Cause
The shadow was defined in `TextStyle` but never rendered - only the main text pass was executed.

### Solution Applied

**Two-Pass Rendering**
- Refactored `render()` method to call `renderPass()` internally
- If `style.isDropShadow()` is true:
  1. First pass: Render shadow at offset position with shadow color
  2. Second pass: Render main text with normal style
- File: `GlSdfTextRenderer.java` (lines ~85-115)

```java
// Shadow pass (if enabled)
if (style.isDropShadow()) {
    Vector2 shadowOffset = style.getShadowOffset();
    Color shadowColor = style.getShadowColor();
    TextStyle shadowStyle = new TextStyle(shadowColor);
    renderPass(text, font, x + shadowOffset.getX(), y + shadowOffset.getY(), 
        shadowStyle, viewMatrix);
}

// Main text pass
renderPass(text, font, x, y, style, viewMatrix);
```

---

## Architecture Note: Platform Separation

Also fixed architecture violation:
- **Moved** LWJGL-dependent code from `modules/graphics` to `platform/desktop`
- **Created** `FontGenerator` interface in `modules/graphics`
- **Renamed** `SdfFontGenerator` → `GlSdfFontGenerator` in `platform/desktop`
- **Implements** `FontGenerator` interface

This ensures clean separation:
- ✅ `modules/graphics` = Platform-agnostic (interfaces, data structures)
- ✅ `platform/desktop` = OpenGL/LWJGL implementation

---

## Testing

Run the demo to see improvements:
```bash
.\gradlew :demo-desktop-learning:run --args="org.pixel.demo.learning.canvas.CanvasTextDemo"
```

Expected results:
- ✅ Smoother text (less jagged edges)
- ✅ Visible drop shadows on "Text with shadow"
- ✅ Better quality at different font sizes

---

## Future Improvements

For production-quality text:
1. **True SDF Generation** - Calculate actual signed distance fields instead of using bitmap alpha
2. **MSDF (Multi-channel SDF)** - Even better quality with sharper corners
3. **Pre-baked Fonts** - Use tools like `msdfgen` offline for best results
4. **Kerning Support** - Proper character pair spacing from font metrics

Current implementation is good for Phase 1 but can be enhanced later!
