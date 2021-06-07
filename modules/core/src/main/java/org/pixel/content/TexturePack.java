/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.content;

import java.util.ArrayList;
import java.util.List;
import org.pixel.commons.lifecycle.Disposable;

import java.util.Map;

/**
 * @author João Filipe Alves
 */
public class TexturePack implements Disposable {

    private final Texture texture;
    private final Map<String, TextureFrame> frameMap;

    /**
     * Constructor
     *
     * @param frameMap
     */
    public TexturePack(Texture texture, Map<String, TextureFrame> frameMap) {
        this.texture = texture;
        this.frameMap = frameMap;
    }

    /**
     * @param frameName
     * @return
     */
    public TextureFrame getFrame(String frameName) {
        return frameMap.get(frameName);
    }

    /**
     * @param frameNames
     * @return
     */
    public List<TextureFrame> getFrames(String... frameNames) {
        var frames = new ArrayList<TextureFrame>();
        for (String frameName : frameNames) {
            var frame = getFrame(frameName);
            if (frame != null) {
                frames.add(frame);
            }
        }

        return frames;
    }

    /**
     * @return
     */
    public Texture getTexture() {
        return texture;
    }

    @Override
    public void dispose() {
        if (this.texture != null) {
            this.texture.dispose();
        }
    }
}
