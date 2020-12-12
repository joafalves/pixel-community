/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.content;

import pixel.commons.lifecycle.Disposable;

import java.util.Map;

/**
 * @author João Filipe Alves
 */
public class TexturePack implements Disposable {

    private Texture texture;
    private Map<String, TextureFrame> frameMap;

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
