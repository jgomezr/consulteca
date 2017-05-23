package org.grameenfoundation.consulteca.utils;

import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Stack;

/**
 * Helper class for creating an XML entity that can be used in an HTTP POST request
 * 
 */
public class XmlEntityBuilder {
    private StringBuilder entity;
    private Stack<String> xmlElementNames;

    public XmlEntityBuilder() {
        this.entity = new StringBuilder();
        this.xmlElementNames = new Stack<String>();
        this.entity.append("<?xml version=\"1.0\"?>");
    }

    public AbstractHttpEntity getEntity() throws UnsupportedEncodingException {
        if (this.xmlElementNames.size() > 0) {
            throw new IllegalStateException("there are still outstanding elements that were not closed with writeEndElement");
        }
        return new StringEntity(this.entity.toString(), HTTP.UTF_8);
    }

    public void writeStartElement(String elementName) {
        writeStartElement(elementName, "", "");
    }

    public void writeStartElement(String elementName, String elementNamespace) {
        writeStartElement(elementName, elementNamespace, "");
    }

    public void writeStartElement(String elementName, String namespaceUri, String namespacePrefix) {
        if (elementName == null || elementName.length() == 0) {
            throw new IllegalArgumentException("elementName must be non-empty");
        }

        if (namespaceUri == null) {
            throw new IllegalArgumentException("namespaceUri cannot be null");
        }

        if (namespacePrefix == null) {
            throw new IllegalArgumentException("namespacePrefix cannot be null");
        }

        HashMap<String, String> attributes = null;
        if (namespaceUri.length() > 0) {
            attributes = new HashMap<String, String>();
            String namespaceAttributeName = "xmlns";
            if (namespacePrefix.length() > 0) {
                namespaceAttributeName += (":" + namespacePrefix);
            }
            attributes.put(namespaceAttributeName, namespaceUri);
        }

        writeStartElement(elementName, attributes);
    }

    /**
     * Overload that accepts a set of attributes. Assumes that the attribute names are prefix-qualified if that is
     * required
     */
    public void writeStartElement(String elementName, HashMap<String, String> attributes) {
        if (elementName == null || elementName.length() == 0) {
            throw new IllegalArgumentException("elementName must be non-empty");
        }

        this.xmlElementNames.push(elementName);
        this.entity.append("<" + elementName);
        if (attributes != null && !attributes.isEmpty()) {
            for (Entry<String, String> attribute : attributes.entrySet()) {
                this.entity.append(" " + attribute.getKey() + "=\"" + attribute.getValue() + "\"");
            }
        }
        this.entity.append(">");
    }

    public void writeText(String text) {
        this.entity.append(XmlHelpers.escapeText(text));
    }

    public void writeEndElement() {
        if (this.xmlElementNames.size() == 0) {
            throw new IllegalStateException("writeEndElement must be matched by an earlier call to writeStartElement");
        }
        String elementName = this.xmlElementNames.pop();
        this.entity.append("</" + elementName + ">");
    }
}
