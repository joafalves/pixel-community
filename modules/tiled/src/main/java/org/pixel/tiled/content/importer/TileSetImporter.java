package org.pixel.tiled.content.importer;

import org.pixel.content.ContentImporter;
import org.pixel.content.ImportContext;
import org.pixel.tiled.content.TileMap;
import org.pixel.tiled.content.TileSet;
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
        Document tmxDoc;

        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            ByteBuffer bb = ctx.getBuffer();

            byte[] array = new byte[bb.limit()];
            bb.get(array, 0, bb.limit());
            bb.flip();

            tmxDoc = documentBuilder.parse(new ByteArrayInputStream(array, bb.position(), bb.limit()));

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();

            return null;
        }

        TileSet tileSet = new TileSet();
        tmxDoc.getDocumentElement().normalize();

        return tileSet;
    }
}
