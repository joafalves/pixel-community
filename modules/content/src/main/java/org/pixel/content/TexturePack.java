/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.content;

import java.util.ArrayList;
import java.util.List;
import org.pixel.commons.lifecycle.Disposable;

import java.util.Map;

public class TexturePack implements Disposable {

    private final Texture texture;
    private final Map<String, TextureFrame> frameMap;

    /**
     * Constructor.
     *
     * @param texture  The source texture.
     * @param frameMap Map containing the frame information.
     */
    public TexturePack(Texture texture, Map<String, TextureFrame> frameMap) {
        this.texture = texture;
        this.frameMap = frameMap;
    }

    /**
     * Get a frame by name.
     *
     * @param frameName The frame name.
     * @return The frame or null if not found.
     */
    public TextureFrame getFrame(String frameName) {
        return frameMap.get(frameName);
    }

    /**
     * Get a list of all the frames by the given names.
     *
     * @param frameNames The frame names.
     * @return The list of frames that match the given names or null if not found.
     */
    public List<TextureFrame> getFrames(String... frameNames) {
        ArrayList<TextureFrame> frames = null;
        for (String frameName : frameNames) {
            var frame = getFrame(frameName);
            if (frame != null) {
                if (frames == null) {
                    frames = new ArrayList<>();
                }

                frames.add(frame);
            }
        }

        return frames;
    }

    /**
     * Get the source texture.
     *
     * @return The source texture.
     */
    public Texture getTexture() {
        return texture;
    }

    /**
     * Dispose the texture pack.
     */
    @Override
    public void dispose() {
        if (this.texture != null) {
            this.texture.dispose();
        }
    }
}
