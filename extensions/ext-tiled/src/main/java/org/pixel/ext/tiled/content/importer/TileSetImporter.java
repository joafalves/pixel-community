package org.pixel.ext.tiled.content.importer;

import org.pixel.commons.data.Pair;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.content.ContentImporter;
import org.pixel.content.ContentImporterInfo;
import org.pixel.content.ImportContext;
import org.pixel.content.Texture;
import org.pixel.content.importer.settings.ContentImporterSettings;
import org.pixel.ext.tiled.content.*;
import org.pixel.ext.tiled.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@ContentImporterInfo(type = TileSet.class, extension = ".tsx")
public class TileSetImporter implements ContentImporter<TileSet> {
    private static final Logger LOG = LoggerFactory.getLogger(TileSetImporter.class);
    private final ObjectCollector collector;
    private final CustomPropertiesCollector propertiesCollector;

    public TileSetImporter() {
        collector = new ObjectCollector();
        propertiesCollector = new CustomPropertiesCollector();
    }

    public TileSetImporter(ObjectCollector collector, CustomPropertiesCollector propertiesCollector) {
        this.collector = collector;
        this.propertiesCollector = propertiesCollector;
    }

    @Override
    public TileSet process(ImportContext ctx) {
        XMLUtils utils = new XMLUtils();
        Document tsxDoc = utils.openXMLDocument(ctx);

        if (tsxDoc == null) {
            return null;
        }

        Element tilesetElement = (Element) tsxDoc.getElementsByTagName("tileset").item(0);

        int tileHeight = Integer.parseInt(tilesetElement.getAttribute("tileheight"));
        int tileWidth = Integer.parseInt(tilesetElement.getAttribute("tilewidth"));
        int columns = Integer.parseInt(tilesetElement.getAttribute("columns"));
        int tileCount = Integer.parseInt(tilesetElement.getAttribute("tilecount"));

        Element image = (Element) tilesetElement.getElementsByTagName("image").item(0);

        String textureFilePath = image.getAttribute("source");

        ContentImporterSettings settings;

        if (ctx.getSettings() instanceof TileMapImporterSettings) {
            settings = ((TileMapImporterSettings) ctx.getSettings()).getTextureImporterSettings();
        } else {
            settings = ctx.getSettings();
        }

        Texture tileSetImage = ctx.getContentManager().load(textureFilePath, Texture.class, settings);

        if (tileSetImage == null) {
            LOG.error("Something went wrong processing the Tile Set texture image.");

            return null;
        }

        TiledCustomProperties customProperties = new CustomPropertiesCollector().collect(tilesetElement);

        TileSet tileSet = new TileSet(tileWidth, tileHeight, tileCount, columns, tileSetImage);
        tileSet.setCustomProperties(customProperties);

        NodeList tiles = tilesetElement.getElementsByTagName("tile");

        for (int i = 0; i < tiles.getLength(); i++) {
            Element tileElement = (Element) tiles.item(i);

            TiledTile tile = new TiledTile();

            NodeList groups = tileElement.getElementsByTagName("objectgroup");

            if (groups.getLength() > 0) {
                NodeList objects = ((Element) groups.item(0)).getElementsByTagName("object");
                HashMap<Integer, TiledObject> map = new HashMap<>();


                for (int j = 0; j < objects.getLength(); j++) {
                    Element object = (Element) objects.item(j);

                    Pair<Integer, TiledObject> pair = collector.collect(object);

                    map.put(pair.getA(), pair.getB());
                }

                tile.setColliders(map);
            }

            NodeList animationList = tileElement.getElementsByTagName("animation");
            TiledAnimation animation = null;

            if (animationList.getLength() > 0) {
                Element animationElement = (Element) animationList.item(0);
                NodeList frameList = animationElement.getElementsByTagName("frame");
                animation = new TiledAnimation();
                List<TiledFrame> frames = new ArrayList<>();

                for (int j = 0; j < frameList.getLength(); j++) {
                    Element frameElement = (Element) frameList.item(j);

                    TiledFrame frame = new TiledFrame();

                    frame.setDuration(Integer.parseInt(frameElement.getAttribute("duration")));
                    frame.setLocalId(Integer.parseInt(frameElement.getAttribute("tileid")));

                    frames.add(frame);
                }

                animation.setFrameList(frames);
            }

            tile.setAnimation(animation);

            int index = Integer.parseInt(tileElement.getAttribute("id"));
            tile.setProperties(propertiesCollector.collect(tileElement));
            tileSet.setTile(index, tile);
        }

        return tileSet;
    }
}
