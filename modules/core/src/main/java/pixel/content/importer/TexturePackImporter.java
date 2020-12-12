/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.content.importer;

import org.json.JSONObject;
import pixel.commons.logger.Logger;
import pixel.commons.logger.LoggerFactory;
import pixel.commons.util.FileUtils;
import pixel.commons.util.TextUtils;
import pixel.content.*;
import pixel.math.Rectangle;
import pixel.math.Vector2;

import java.util.HashMap;

/**
 * @author João Filipe Alves
 */
@ContentImporterInfo(type = TexturePack.class, extension = ".json")
public class TexturePackImporter implements ContentImporter<TexturePack> {

    private static final Logger log = LoggerFactory.getLogger(TexturePackImporter.class);

    @Override
    public TexturePack process(ImportContext ctx) {
        String src = TextUtils.convertToString(ctx.getBuffer());
        JSONObject json = new JSONObject(src);
        JSONObject frames = json.getJSONObject("frames");
        if (frames == null) {
            log.warn("Unable to load texture pack due to invalid format");
            return null;
        }

        HashMap<String, TextureFrame> frameMap = new HashMap<>();
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

            frameMap.put(key, new TextureFrame(source, pivot));
        });

        // when available, attempt to load the associated texture pack meta:
        Texture texture = null;
        if (json.has("meta")) {
            JSONObject meta = json.getJSONObject("meta");
            if (meta.has("image")) {
                texture = ctx.getContentManager().load(FileUtils.getParentDirectory(ctx.getFilepath()) +
                        FileUtils.FILE_SEPARATOR + meta.getString("image"), Texture.class);
            }
        }

        return frameMap.size() > 0 ? new TexturePack(texture, frameMap) : null;
    }
}
