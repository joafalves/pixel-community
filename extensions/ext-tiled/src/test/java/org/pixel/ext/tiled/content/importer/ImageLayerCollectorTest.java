package org.pixel.ext.tiled.content.importer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pixel.content.ContentManager;
import org.pixel.content.ImportContext;
import org.pixel.content.Texture;
import org.pixel.ext.tiled.content.TiledMap;
import org.pixel.ext.tiled.content.TiledImageLayer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

class ImageLayerCollectorTest {
    @Test
    void processCase1() throws ParserConfigurationException, IOException, SAXException {
        ImageLayerCollector processor = new ImageLayerCollector();
        Texture texture = Mockito.mock(Texture.class);
        ImportContext ctx = Mockito.mock(ImportContext.class);
        ContentManager manager = Mockito.mock(ContentManager.class);
        TiledMap tileMap = Mockito.mock(TiledMap.class);

        Mockito.when(ctx.getContentManager()).thenReturn(manager);
        Mockito.when(manager.load(Mockito.anyString(), Mockito.eq(Texture.class), Mockito.any())).thenReturn(texture);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        String fileName = "imagemap.tmx";

        Document document = documentBuilder.parse(this.getClass().getClassLoader().getResourceAsStream(fileName));

        NodeList list = document.getElementsByTagName("imagelayer");

        TiledImageLayer layer = (TiledImageLayer) processor.collect(tileMap, (Element) list.item(0), ctx);

        Assertions.assertEquals(0.5f, layer.getOffsetX());
        Assertions.assertEquals(0.2f, layer.getOffsetY());

        Assertions.assertEquals("this is test", layer.getCustomProperties().get("2"));

        Assertions.assertEquals(texture, layer.getImage());
    }
}