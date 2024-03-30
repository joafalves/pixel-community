/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.content.importer;

import java.util.HashMap;
import org.json.JSONObject;
import org.pixel.commons.AttributeMap;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.commons.util.FileUtils;
import org.pixel.commons.util.TextUtils;
import org.pixel.content.ContentImporter;
import org.pixel.content.ContentImporterInfo;
import org.pixel.content.ImportContext;
import org.pixel.content.Texture;
import org.pixel.content.TextureFrame;
import org.pixel.content.TexturePack;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;

@ContentImporterInfo(type = TexturePack.class, extension = ".json")
public class TexturePackImporter implements ContentImporter<TexturePack> {

    private static final Logger log = LoggerFactory.getLogger(TexturePackImporter.class);

    @Override
    public TexturePack process(ImportContext ctx) {
        String src = TextUtils.convertBufferToString(ctx.getBuffer());
        JSONObject json = new JSONObject(src);
        JSONObject frames = json.getJSONObject("frames");
        if (frames == null) {
            log.warn("Unable to load texture pack due to invalid format.");
            return null;
        }

        // when available, attempt to load the associated texture pack meta:
        Texture texture = null;
        if (json.has("meta")) {
            JSONObject meta = json.getJSONObject("meta");
            if (meta.has("image")) {
                texture = ctx.getContentManager().load(FileUtils.getParentDirectory(ctx.getFilepath()) +
                        FileUtils.FILE_SEPARATOR + meta.getString("image"), Texture.class);
                
                if (texture == null) {
                    log.warn("Unable to load texture pack due to missing texture file.");
                    return null;
                }
            }
        }

        final HashMap<String, TextureFrame> frameMap = new HashMap<>();
        final Texture frameTexture = texture;
        frames.keys().forEachRemaining(key -> {
            JSONObject frameInfo = frames.getJSONObject(key);
            JSONObject frameSrc = frameInfo.getJSONObject("frame");
            JSONObject pivotSrc = frameInfo.has("pivot") ? frameInfo.getJSONObject("pivot") : null;
            Rectangle source = new Rectangle(
                    frameSrc.getInt("x"), frameSrc.getInt("y"),
                    frameSrc.getInt("w"), frameSrc.getInt("h"));

            Vector2 pivot;
            if (pivotSrc != null) {
                pivot = new Vector2(pivotSrc.getFloat("x"), pivotSrc.getFloat("y"));

            } else {
                pivot = new Vector2(0, 0);
            }

            TextureFrame textureFrame = new TextureFrame(frameTexture, source, pivot);
            if (frameInfo.has("attributes")) { // does it have attributes?
                var attributeMap = new AttributeMap();
                var attributes = frameInfo.getJSONObject("attributes");
                attributes.keys().forEachRemaining(attrKey -> attributeMap.put(attrKey, attributes.get(attrKey)));
                if (!attributeMap.isEmpty()) {
                    textureFrame.setAttributes(attributeMap);
                }
            }

            frameMap.put(key, textureFrame);
        });

        return frameMap.size() > 0 ? new TexturePack(texture, frameMap) : null;
    }
}
