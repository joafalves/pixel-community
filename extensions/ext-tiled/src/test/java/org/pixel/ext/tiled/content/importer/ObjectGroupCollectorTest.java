package org.pixel.ext.tiled.content.importer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pixel.content.ImportContext;
import org.pixel.ext.tiled.content.TiledMap;
import org.pixel.ext.tiled.content.TiledObject;
import org.pixel.ext.tiled.content.TiledObjectGroup;
import org.pixel.ext.tiled.content.TiledTileObject;
import org.pixel.math.MathHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Iterator;

class ObjectGroupCollectorTest {
    @Test
    void process() throws ParserConfigurationException, IOException, SAXException {
        ObjectGroupCollector processor = new ObjectGroupCollector();
        TiledMap tileMap = Mockito.mock(TiledMap.class);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        String fileName = "untitled.tmx";

        Document document = documentBuilder.parse(this.getClass().getClassLoader().getResourceAsStream(fileName));

        NodeList list = document.getElementsByTagName("objectgroup");

        TiledObjectGroup group = (TiledObjectGroup) processor.collect(tileMap, (Element) list.item(0), Mockito.mock(ImportContext.class));

        Assertions.assertEquals("1", group.getCustomProperties().get("1"));
        Assertions.assertEquals(7.08333, group.getOffsetX(), 0.001);
        Assertions.assertEquals(2.04167, group.getOffsetY(), 0.001);

        Assertions.assertEquals(7, group.getObjects().size());

        Assertions.assertTrue(group.getObjects().containsKey(1));
        Assertions.assertTrue(group.getObjects().get(1) instanceof TiledTileObject);
        Assertions.assertEquals(1, ((TiledTileObject) group.getObjects().get(1)).getgID());
        Assertions.assertEquals(518.917, group.getObjects().get(1).getPosition().getX(), 0.001);
        Assertions.assertEquals(272.167, group.getObjects().get(1).getPosition().getY(), 0.001);
        Assertions.assertEquals(40.3333, ((TiledTileObject) group.getObjects().get(1)).getWidth(), 0.001);
        Assertions.assertEquals(26, ((TiledTileObject) group.getObjects().get(1)).getHeight());
        Assertions.assertEquals(MathHelper.toRadians(2), group.getObjects().get(1).getRotation());

        Assertions.assertEquals(0, group.getObjects().get(10).getRotation());

        Assertions.assertFalse(group.getObjects().get(11) instanceof TiledTileObject);

        Assertions.assertEquals("1", group.getObjects().get(18).getCustomProperties().get("1"));
    }

    @Test
    void processIndex() throws ParserConfigurationException, IOException, SAXException {
        ObjectGroupCollector processor = new ObjectGroupCollector();
        TiledMap tileMap = Mockito.mock(TiledMap.class);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        String fileName = "groupindex.tmx";

        Document document = documentBuilder.parse(this.getClass().getClassLoader().getResourceAsStream(fileName));

        NodeList list = document.getElementsByTagName("objectgroup");

        TiledObjectGroup group = (TiledObjectGroup) processor.collect(tileMap, (Element) list.item(0), Mockito.mock(ImportContext.class));

        Assertions.assertEquals(0, group.getOffsetX(), 0.001);
        Assertions.assertEquals(0, group.getOffsetY(), 0.001);

        Assertions.assertEquals(2, group.getObjects().size());

        Assertions.assertTrue(group.getObjects().containsKey(1));
        Iterator<Integer> keySetIt = group.getObjects().keySet().iterator();
        Iterator<TiledObject> valuesIt = group.getObjects().values().iterator();
        Assertions.assertEquals(1, keySetIt.next());
        Assertions.assertEquals(2, keySetIt.next());
    }

    @Test
    void processTopDown() throws ParserConfigurationException, IOException, SAXException {
        ObjectGroupCollector processor = new ObjectGroupCollector();
        TiledMap tileMap = Mockito.mock(TiledMap.class);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        String fileName = "grouptopdown.tmx";

        Document document = documentBuilder.parse(this.getClass().getClassLoader().getResourceAsStream(fileName));

        NodeList list = document.getElementsByTagName("objectgroup");

        TiledObjectGroup group = (TiledObjectGroup) processor.collect(tileMap, (Element) list.item(0), Mockito.mock(ImportContext.class));

        Assertions.assertEquals(0, group.getOffsetX(), 0.001);
        Assertions.assertEquals(0, group.getOffsetY(), 0.001);

        Assertions.assertEquals(2, group.getObjects().size());

        Assertions.assertTrue(group.getObjects().containsKey(1));
        Iterator<Integer> keySetIt = group.getObjects().keySet().iterator();
        Iterator<TiledObject> valuesIt = group.getObjects().values().iterator();
        Assertions.assertEquals(2, keySetIt.next());
        Assertions.assertEquals(1, keySetIt.next());
    }
}