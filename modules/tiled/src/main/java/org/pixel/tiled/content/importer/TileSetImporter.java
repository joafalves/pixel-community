package org.pixel.tiled.content.importer;

import org.mockito.internal.matchers.Null;
import org.pixel.content.ContentImporter;
import org.pixel.content.ImportContext;
import org.pixel.tiled.content.TileMap;
import org.pixel.tiled.content.TileSet;
import org.pixel.tiled.content.utils.XMLUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class TileSetImporter implements ContentImporter<TileSet> {
    @Override
    public TileSet process(ImportContext ctx) {
        XMLUtils utils = new XMLUtils();
        Document tsxDoc = utils.openXMLDocument(ctx);

        if(tsxDoc == null) {
            return null;
        }

        //TileSet tileSet = new TileSet();

        return null;
    }
}
