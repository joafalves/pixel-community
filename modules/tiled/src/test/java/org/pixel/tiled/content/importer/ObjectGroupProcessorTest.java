package org.pixel.tiled.content.importer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.pixel.content.ImportContext;
import org.pixel.tiled.content.TileMap;
import org.pixel.tiled.content.TiledObject;
import org.pixel.tiled.content.TiledObjectGroup;
import org.pixel.tiled.content.TiledTileObject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

class ObjectGroupProcessorTest {
    @Test
    void process() throws ParserConfigurationException, IOException, SAXException {
        ObjectGroupProcessor processor = new ObjectGroupProcessor();
        TileMap tileMap = Mockito.mock(TileMap.class);
        ArgumentCaptor<List<TiledObjectGroup>> captor = ArgumentCaptor.forClass(List.class);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        String fileName = "untitled.tmx";

        Document document = documentBuilder.parse(this.getClass().getClassLoader().getResourceAsStream(fileName));

        processor.process(tileMap, document, Mockito.mock(ImportContext.class));

        Mockito.verify(tileMap).setObjectGroups(captor.capture());
        Assertions.assertEquals(1, captor.getValue().size());

        Assertions.assertEquals("1", captor.getValue().get(0).getCustomProperties().get("1"));
        Assertions.assertEquals(7.08333, captor.getValue().get(0).getOffsetX(), 0.001);
        Assertions.assertEquals(2.04167, captor.getValue().get(0).getOffsetY(), 0.001);

        Assertions.assertEquals(7, captor.getValue().get(0).getObjects().size());

        Assertions.assertTrue(captor.getValue().get(0).getObjects().containsKey(1));
        Assertions.assertTrue(captor.getValue().get(0).getObjects().get(1) instanceof TiledTileObject);
        Assertions.assertEquals(1, ((TiledTileObject) captor.getValue().get(0).getObjects().get(1)).getgID());
        Assertions.assertEquals(518.917, captor.getValue().get(0).getObjects().get(1).getPosition().getX(), 0.001);
        Assertions.assertEquals(272.167, captor.getValue().get(0).getObjects().get(1).getPosition().getY(), 0.001);
        Assertions.assertEquals(40.3333, captor.getValue().get(0).getObjects().get(1).getWidth(), 0.001);
        Assertions.assertEquals(26, captor.getValue().get(0).getObjects().get(1).getHeight());
        Assertions.assertEquals(2, captor.getValue().get(0).getObjects().get(1).getRotation());

        Assertions.assertEquals(0, captor.getValue().get(0).getObjects().get(10).getRotation());

        Assertions.assertFalse(captor.getValue().get(0).getObjects().get(11) instanceof TiledTileObject);

        Assertions.assertEquals("1", captor.getValue().get(0).getObjects().get(18).getCustomProperties().get("1"));
    }

    @Test
    void processIndex() throws ParserConfigurationException, IOException, SAXException {
        ObjectGroupProcessor processor = new ObjectGroupProcessor();
        TileMap tileMap = Mockito.mock(TileMap.class);
        ArgumentCaptor<List<TiledObjectGroup>> captor = ArgumentCaptor.forClass(List.class);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        String fileName = "groupindex.tmx";

        Document document = documentBuilder.parse(this.getClass().getClassLoader().getResourceAsStream(fileName));

        processor.process(tileMap, document, Mockito.mock(ImportContext.class));

        Mockito.verify(tileMap).setObjectGroups(captor.capture());
        Assertions.assertEquals(1, captor.getValue().size());

        Assertions.assertEquals(0, captor.getValue().get(0).getOffsetX(), 0.001);
        Assertions.assertEquals(0, captor.getValue().get(0).getOffsetY(), 0.001);

        Assertions.assertEquals(2, captor.getValue().get(0).getObjects().size());

        Assertions.assertTrue(captor.getValue().get(0).getObjects().containsKey(1));
        Iterator<Integer> keySetIt = captor.getValue().get(0).getObjects().keySet().iterator();
        Iterator<TiledObject> valuesIt = captor.getValue().get(0).getObjects().values().iterator();
        Assertions.assertEquals(1, keySetIt.next());
        Assertions.assertEquals(2, keySetIt.next());
    }

    @Test
    void processTopDown() throws ParserConfigurationException, IOException, SAXException {
        ObjectGroupProcessor processor = new ObjectGroupProcessor();
        TileMap tileMap = Mockito.mock(TileMap.class);
        ArgumentCaptor<List<TiledObjectGroup>> captor = ArgumentCaptor.forClass(List.class);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        String fileName = "grouptopdown.tmx";

        Document document = documentBuilder.parse(this.getClass().getClassLoader().getResourceAsStream(fileName));

        processor.process(tileMap, document, Mockito.mock(ImportContext.class));

        Mockito.verify(tileMap).setObjectGroups(captor.capture());
        Assertions.assertEquals(1, captor.getValue().size());

        Assertions.assertEquals(0, captor.getValue().get(0).getOffsetX(), 0.001);
        Assertions.assertEquals(0, captor.getValue().get(0).getOffsetY(), 0.001);

        Assertions.assertEquals(2, captor.getValue().get(0).getObjects().size());

        Assertions.assertTrue(captor.getValue().get(0).getObjects().containsKey(1));
        Iterator<Integer> keySetIt = captor.getValue().get(0).getObjects().keySet().iterator();
        Iterator<TiledObject> valuesIt = captor.getValue().get(0).getObjects().values().iterator();
        Assertions.assertEquals(2, keySetIt.next());
        Assertions.assertEquals(1, keySetIt.next());
    }
}