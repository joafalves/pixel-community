package org.pixel.tiled.content.utils;

import org.pixel.content.ImportContext;
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

public class XMLUtils {
    public Document openXMLDocument(ImportContext ctx) {
        Document doc;

        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            ByteBuffer bb = ctx.getBuffer();

            byte[] array = new byte[bb.limit()];
            bb.get(array, 0, bb.limit());
            bb.flip();

            doc = documentBuilder.parse(new ByteArrayInputStream(array, bb.position(), bb.limit()));

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();

            return null;
        }

        doc.getDocumentElement().normalize();

        return doc;
    }
}
