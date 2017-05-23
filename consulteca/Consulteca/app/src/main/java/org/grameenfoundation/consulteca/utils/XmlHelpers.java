package org.grameenfoundation.consulteca.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;

/**
 * Helper methods that can be used for manipulating XML documents
 *
 */
public class XmlHelpers {
    static DocumentBuilderFactory documentBuilderFactory = CreateDocumentBuilderFactory();

    private static DocumentBuilderFactory CreateDocumentBuilderFactory() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // need to set this to true so that getLocalName and getNamespaceURI function correctly
        factory.setNamespaceAware(true);
        return factory;
    }

    /**
     * Convert the given string into an XML DOM
     *
     * @deprecated Takes up too much memory. Use SAXParser instead
     */
    public static Document parseXml(String xmlString) throws SAXException, IOException, ParserConfigurationException {
        return parseXml(new StringReader(xmlString));
    }

    /**
     * Convert the given reader into an XML DOM
     *
     * @deprecated Do not use. Takes up too much memory. Use SAXParser instead
     */
    public static Document parseXml(Reader reader) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        InputSource inputSource = new InputSource(reader);
        return documentBuilder.parse(inputSource);
    }

    /**
     * Convert the given stream into an XML DOM
     */
    public static Document parseXml(InputStream stream) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        InputSource inputSource = new InputSource(stream);
        return documentBuilder.parse(inputSource);
    }

    /**
     * Uses our shared document builder factory to create a new DOM
     */
    public static Document createDocument() throws ParserConfigurationException {
        return documentBuilderFactory.newDocumentBuilder().newDocument();
    }

    /**
     * Escapes an XML string (i.e. <br/>
     * becomes &lt;br/&gt;) There are 5 entities of interest (<, >, ", ', and &)
     *
     * Unfortunately there are no built-in Java utilities for this, so we had to roll our own
     */
    public static String escapeText(String rawText) {
        StringBuilder escapedText = new StringBuilder();
        StringCharacterIterator characterIterator = new StringCharacterIterator(rawText);
        char currentCharacter = characterIterator.current();
        while (currentCharacter != CharacterIterator.DONE) {
            switch (currentCharacter) {
                case '<':
                    escapedText.append("&lt;");
                    break;

                case '>':
                    escapedText.append("&gt;");
                    break;

                case '"':
                    escapedText.append("&quot;");
                    break;

                case '\'':
                    escapedText.append("&apos;");
                    break;

                case '&':
                    escapedText.append("&amp;");
                    break;

                default:
                    escapedText.append(currentCharacter);
                    break;
            }
            currentCharacter = characterIterator.next();
        }
        return escapedText.toString();
    }

    /**
     * Get the content value for the given XML element
     */
    public static String getContent(Element xmlElement) {
        if (xmlElement == null) {
            throw new IllegalArgumentException("xmlElement cannot be null");
        }
        StringBuilder content = new StringBuilder();
        for (Node childNode = xmlElement.getFirstChild(); childNode != null; childNode = childNode.getNextSibling()) {
            if (childNode.getNodeType() == Node.TEXT_NODE) {
                content.append(childNode.getNodeValue());
            }
        }
        return content.toString();
    }

    /**
     * Get list content for the given XML element, of the form <Element>content</Element>...
     */
    public static ArrayList<String> getListContent(Element listElement, String listItemName, String listItemNamespace) {
        if (listElement == null) {
            throw new IllegalArgumentException("listElement cannot be null");
        }
        if (listItemName == null || listItemName.length() == 0) {
            throw new IllegalArgumentException("listItemName cannot be null or empty");
        }
        if (listItemNamespace == null) {
            throw new IllegalArgumentException("listItemNamespace cannot be null");
        }
        ArrayList<String> listContent = new ArrayList<String>();
        for (Node childNode = listElement.getFirstChild(); childNode != null; childNode = childNode.getNextSibling()) {
            if (childNode.getNodeType() == Node.ELEMENT_NODE && listItemName.equals(childNode.getLocalName())
                    && listItemNamespace.equals(childNode.getNamespaceURI())) {
                listContent.add(getContent((Element)childNode));
            }
        }
        return listContent;
    }

    public static Boolean writeXmlToTempFile(InputStream xmlStream, String filePath, String closingTag) throws IOException {
        Boolean downloadSuccessful = false;
        File tempFile = new File(filePath);
        FileOutputStream stream = new FileOutputStream(tempFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(xmlStream));

        String line;
        while ((line = reader.readLine()) != null) {
            stream.write(line.getBytes());
            // Check if we downloaded successfully
            if (downloadSuccessful == false && line.toLowerCase().contains(closingTag.toLowerCase())) {
                downloadSuccessful = true;
            }
        }
        stream.close();
        reader.close();

        return downloadSuccessful;
    }
}
