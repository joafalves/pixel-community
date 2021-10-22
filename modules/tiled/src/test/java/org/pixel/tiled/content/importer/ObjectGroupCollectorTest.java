package org.pixel.tiled.content.importer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.pixel.math.MathHelper;
import org.pixel.tiled.content.TileMap;
import org.pixel.tiled.content.TiledObject;
import org.pixel.tiled.content.TiledObjectGroup;
import org.pixel.tiled.content.TiledTileObject;
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
        TileMap tileMap = Mockito.mock(TileMap.class);
        ArgumentCaptor<TiledObjectGroup> captor = ArgumentCaptor.forClass(TiledObjectGroup.class);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        String fileName = "untitled.tmx";

        Document document = documentBuilder.parse(this.getClass().getClassLoader().getResourceAsStream(fileName));

        NodeList list = document.getElementsByTagName("objectgroup");

        processor.process(tileMap, (Element) list.item(0));

        Mockito.verify(tileMap).addLayer(captor.capture());

        Assertions.assertEquals("1", captor.getValue().getCustomProperties().get("1"));
        Assertions.assertEquals(7.08333, captor.getValue().getOffsetX(), 0.001);
        Assertions.assertEquals(2.04167, captor.getValue().getOffsetY(), 0.001);

        Assertions.assertEquals(7, captor.getValue().getObjects().size());

        Assertions.assertTrue(captor.getValue().getObjects().containsKey(1));
        Assertions.assertTrue(captor.getValue().getObjects().get(1) instanceof TiledTileObject);
        Assertions.assertEquals(1, ((TiledTileObject) captor.getValue().getObjects().get(1)).getgID());
        Assertions.assertEquals(518.917, captor.getValue().getObjects().get(1).getPosition().getX(), 0.001);
        Assertions.assertEquals(272.167, captor.getValue().getObjects().get(1).getPosition().getY(), 0.001);
        Assertions.assertEquals(40.3333, captor.getValue().getObjects().get(1).getWidth(), 0.001);
        Assertions.assertEquals(26, captor.getValue().getObjects().get(1).getHeight());
        Assertions.assertEquals(MathHelper.degToRad(2), captor.getValue().getObjects().get(1).getRotation());

        Assertions.assertEquals(0, captor.getValue().getObjects().get(10).getRotation());

        Assertions.assertFalse(captor.getValue().getObjects().get(11) instanceof TiledTileObject);

        Assertions.assertEquals("1", captor.getValue().getObjects().get(18).getCustomProperties().get("1"));
    }

    @Test
    void processIndex() throws ParserConfigurationException, IOException, SAXException {
        ObjectGroupCollector processor = new ObjectGroupCollector();
        TileMap tileMap = Mockito.mock(TileMap.class);
        ArgumentCaptor<TiledObjectGroup> captor = ArgumentCaptor.forClass(TiledObjectGroup.class);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        String fileName = "groupindex.tmx";

        Document document = documentBuilder.parse(this.getClass().getClassLoader().getResourceAsStream(fileName));

        NodeList list = document.getElementsByTagName("objectgroup");

        processor.process(tileMap, (Element) list.item(0));

        Mockito.verify(tileMap).addLayer(captor.capture());

        Assertions.assertEquals(0, captor.getValue().getOffsetX(), 0.001);
        Assertions.assertEquals(0, captor.getValue().getOffsetY(), 0.001);

        Assertions.assertEquals(2, captor.getValue().getObjects().size());

        Assertions.assertTrue(captor.getValue().getObjects().containsKey(1));
        Iterator<Integer> keySetIt = captor.getValue().getObjects().keySet().iterator();
        Iterator<TiledObject> valuesIt = captor.getValue().getObjects().values().iterator();
        Assertions.assertEquals(1, keySetIt.next());
        Assertions.assertEquals(2, keySetIt.next());
    }

    @Test
    void processTopDown() throws ParserConfigurationException, IOException, SAXException {
        ObjectGroupCollector processor = new ObjectGroupCollector();
        TileMap tileMap = Mockito.mock(TileMap.class);
        ArgumentCaptor<TiledObjectGroup> captor = ArgumentCaptor.forClass(TiledObjectGroup.class);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        String fileName = "grouptopdown.tmx";

        Document document = documentBuilder.parse(this.getClass().getClassLoader().getResourceAsStream(fileName));

        NodeList list = document.getElementsByTagName("objectgroup");

        processor.process(tileMap, (Element) list.item(0));

        Mockito.verify(tileMap).addLayer(captor.capture());

        Assertions.assertEquals(0, captor.getValue().getOffsetX(), 0.001);
        Assertions.assertEquals(0, captor.getValue().getOffsetY(), 0.001);

        Assertions.assertEquals(2, captor.getValue().getObjects().size());

        Assertions.assertTrue(captor.getValue().getObjects().containsKey(1));
        Iterator<Integer> keySetIt = captor.getValue().getObjects().keySet().iterator();
        Iterator<TiledObject> valuesIt = captor.getValue().getObjects().values().iterator();
        Assertions.assertEquals(2, keySetIt.next());
        Assertions.assertEquals(1, keySetIt.next());
    }
}