package org.pixel.ext.tiled.content.importer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pixel.content.ImportContext;
import org.pixel.ext.tiled.content.TiledLayerGroup;
import org.pixel.ext.tiled.content.TiledTileLayer;
import org.pixel.ext.tiled.content.TiledMap;
import org.pixel.ext.tiled.content.TiledObjectGroup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

class LayerGroupCollectorTest {
    @Test
    void processCase1() throws ParserConfigurationException, IOException, SAXException {
        LayerGroupCollector processor = new LayerGroupCollector();
        TiledMap tileMap = Mockito.mock(TiledMap.class);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        String fileName = "group1.tmx";

        Document document = documentBuilder.parse(this.getClass().getClassLoader().getResourceAsStream(fileName));

        NodeList list = document.getElementsByTagName("group");

        TiledLayerGroup group = (TiledLayerGroup) processor.collect(tileMap, (Element) list.item(0), Mockito.mock(ImportContext.class));

        Assertions.assertEquals(5, group.getOffsetX());
        Assertions.assertEquals(3, group.getOffsetY());

        Assertions.assertEquals("hello", group.getCustomProperties().get("1"));

        Assertions.assertEquals(2, group.getLayers().size());
        Assertions.assertTrue(group.getLayers().get(0) instanceof TiledTileLayer);
        Assertions.assertEquals(2 + 3, group.getLayers().get(0).getOffsetY());
        Assertions.assertEquals(1 + 5, group.getLayers().get(1).getOffsetX());
        Assertions.assertTrue(group.getLayers().get(1) instanceof TiledObjectGroup);
    }
}