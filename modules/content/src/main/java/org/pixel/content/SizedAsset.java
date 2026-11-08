package org.pixel.content;

/**
 * An asset that knows roughly how much memory it keeps (GPU and CPU), e.g. for the dev tools' Assets tab.
 */
public interface SizedAsset {

    /**
     * @return Approximate bytes kept by this asset (a texture's pixels, a sound's samples, ...), not counting assets
     * it refers to that are loaded on their own (e.g. a texture pack's texture).
     */
    long getMemorySize();
}
