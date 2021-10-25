package org.pixel.tiled.content.importer;

import org.pixel.tiled.content.TiledCustomProperties;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Objects;

public class CustomPropertiesCollector {
    public TiledCustomProperties collect(Element xmlDoc) {
        NodeList properties = xmlDoc.getElementsByTagName("properties");

        TiledCustomProperties customProperties = new TiledCustomProperties();

        if(properties == null) return customProperties;

        if (properties.getLength() != 0 && properties.item(0).getNodeType() == Node.ELEMENT_NODE) {
            NodeList propertyList = ((Element) properties.item(0)).getElementsByTagName("property");

            for (int i = 0; i < propertyList.getLength(); i++) {
                Node propertyNode = propertyList.item(i);

                if (propertyNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element property = (Element) propertyNode;

                    if (Objects.equals(property.getAttribute("value"), "")) {
                        customProperties.put(property.getAttribute("name"), property.getTextContent());
                    } else {
                        customProperties.put(property.getAttribute("name"), property.getAttribute("value"));
                    }
                }
            }
        }

        return customProperties;
    }
}
