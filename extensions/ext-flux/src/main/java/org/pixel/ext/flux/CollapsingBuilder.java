package org.pixel.ext.flux;

/**
 * Fluent builder for collapsing section configuration.
 *
 * <p>Collapsing sections allow you to hide/show content with a clickable header.
 *
 * <p>Example usage:
 * <pre>
 * if (gui.collapsing("section1", "Section Title")
 *         .withDefaultOpen(true)
 *         .begin()) {
 *     // Content only rendered when open
 *     gui.label("Hidden content");
 * }
 * </pre>
 */
public class CollapsingBuilder {

    private final Flux flux;
    private final String id;
    private final String title;

    // Optional properties
    private boolean defaultOpen = false;

    /**
     * Create a new collapsing section builder.
     *
     * @param flux  The Flux instance
     * @param id    Unique identifier
     * @param title Section title
     */
    public CollapsingBuilder(Flux flux, String id, String title) {
        this.flux = flux;
        this.id = id;
        this.title = title;
    }

    /**
     * Set whether this section should be open by default (first frame).
     *
     * @param open True to start open
     * @return This builder for chaining
     */
    public CollapsingBuilder withDefaultOpen(boolean open) {
        this.defaultOpen = open;
        return this;
    }

    /**
     * Begin the collapsing section with configured properties.
     *
     * @return True if the section is currently open (render content)
     */
    public boolean begin() {
        return flux.beginCollapsingInternal(id, title, defaultOpen);
    }
}
