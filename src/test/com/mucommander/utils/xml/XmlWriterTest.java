package com.mucommander.utils.xml;

import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Runs test on the {@link XmlWriter} class.
 * @author Nicolas Rinaudo
 */
public class XmlWriterTest {
    private static final String ROOT_ELEMENT       = "root";
    private static final String ENTITIES_ATTRIBUTE = "entities";
    private static final String ENTITIES_STRING    = "&\"'<>";



    // - XML reading -----------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Reads the content of the specified byte array in a DOM Document.
     * @param  bytes       content of the XML document.
     * @return             the content of the specified byte array in a DOM Document.
     * @throws Exception if any error occurs.
     */
    private Document getDocument(byte[] bytes) throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(bytes));
    }



    // - JUnit tests -----------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Makes sure that XML entities are escaped properly.
     * @throws IOException if an error occurs.
     */
    @Test
    public void testXmlEntities() throws Exception {
        XmlWriter             writer;
        XmlAttributes         attributes;
        ByteArrayOutputStream out;
        Node                  node;
        Element               element;

        // Creates an XML document with CDATA and attributes that need escaping.
        writer     = new XmlWriter(out = new ByteArrayOutputStream());
        attributes = new XmlAttributes();
        attributes.add(ENTITIES_ATTRIBUTE, ENTITIES_STRING);
        writer.startElement(ROOT_ELEMENT, attributes);
        writer.writeCData(ENTITIES_STRING);
        writer.endElement(ROOT_ELEMENT);
        writer.close();

        // Reads the XML stream.
        element = getDocument(out.toByteArray()).getDocumentElement();

        // Makes sure the entities were properly escaped in the XML attribute.
        assert element.hasAttribute(ENTITIES_ATTRIBUTE);
        assert element.getAttribute(ENTITIES_ATTRIBUTE).equals(ENTITIES_STRING);

        // Looks for the CDATA.
        node = element.getFirstChild();
        while(node != null && node.getNodeType() != Node.TEXT_NODE)
            node = node.getNextSibling();

        // Makes sure we found the CDATA and that it is equal to ENTITIES_STRING.
        assert node != null;
        assert node.getNodeValue().equals(ENTITIES_STRING);
    }
}
