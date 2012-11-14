/*
 * JBoss, Home of Professional Open Source. Copyright 2008, Red Hat Middleware LLC, and individual contributors as
 * indicated by the @author tags. See the copyright.txt file in the distribution for a full listing of individual
 * contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this software; if not, write to
 * the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF site:
 * http://www.fsf.org.
 */
package org.picketlink.identity.federation.core.util;

import java.io.OutputStream;
import java.io.Writer;
import java.util.Stack;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;

import org.picketlink.identity.federation.PicketLinkLogger;
import org.picketlink.identity.federation.PicketLinkLoggerFactory;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.wstrust.WSTrustConstants;
import org.picketlink.identity.xmlsec.w3.xmldsig.DSAKeyValueType;
import org.picketlink.identity.xmlsec.w3.xmldsig.KeyInfoType;
import org.picketlink.identity.xmlsec.w3.xmldsig.KeyValueType;
import org.picketlink.identity.xmlsec.w3.xmldsig.RSAKeyValueType;
import org.picketlink.identity.xmlsec.w3.xmldsig.X509CertificateType;
import org.picketlink.identity.xmlsec.w3.xmldsig.X509DataType;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Utility class that deals with StAX
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 19, 2010
 */
public class StaxUtil {
    
    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();
    
    private static ThreadLocal<Stack<String>> registeredNSStack = new ThreadLocal<Stack<String>>();

    /**
     * Flush the stream writer
     *
     * @param writer
     * @throws ProcessingException
     */
    public static void flush(XMLStreamWriter writer) throws ProcessingException {
        try {
            writer.flush();
        } catch (XMLStreamException e) {
            throw logger.processingError(e);
        }
    }

    /**
     * Get an {@code XMLEventWriter}
     *
     * @param outStream
     * @return
     * @throws ProcessingException
     */
    public static XMLEventWriter getXMLEventWriter(final OutputStream outStream) throws ProcessingException {
        XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
        try {
            return xmlOutputFactory.createXMLEventWriter(outStream, "UTF-8");
        } catch (XMLStreamException e) {
            throw logger.processingError(e);
        }
    }

    /**
     * Get an {@code XMLStreamWriter}
     *
     * @param outStream
     * @return
     * @throws ProcessingException
     */
    public static XMLStreamWriter getXMLStreamWriter(final OutputStream outStream) throws ProcessingException {
        XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
        try {
            return xmlOutputFactory.createXMLStreamWriter(outStream, "UTF-8");
        } catch (XMLStreamException e) {
            throw logger.processingError(e);
        }
    }

    /**
     * Get an {@code XMLStreamWriter}
     *
     * @param writer {@code Writer}
     * @return
     * @throws ProcessingException
     */
    public static XMLStreamWriter getXMLStreamWriter(final Writer writer) throws ProcessingException {
        XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
        try {
            return xmlOutputFactory.createXMLStreamWriter(writer);
        } catch (XMLStreamException e) {
            throw logger.processingError(e);
        }
    }

    public static XMLStreamWriter getXMLStreamWriter(final Result result) throws ProcessingException {
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        try {
            return factory.createXMLStreamWriter(result);
        } catch (XMLStreamException xe) {
            throw logger.processingError(xe);
        }
    }

    /**
     * Set a prefix
     *
     * @param writer
     * @param prefix
     * @param nsURI
     * @throws ProcessingException
     */
    public static void setPrefix(XMLStreamWriter writer, String prefix, String nsURI) throws ProcessingException {
        try {
            writer.setPrefix(prefix, nsURI);
        } catch (XMLStreamException e) {
            throw logger.processingError(e);
        }
    }

    /**
     * Write an attribute
     *
     * @param writer
     * @param attributeName QName of the attribute
     * @param attributeValue
     * @throws ProcessingException
     */
    public static void writeAttribute(XMLStreamWriter writer, String attributeName, QName attributeValue)
            throws ProcessingException {
        writeAttribute(writer, attributeName, attributeValue.toString());
    }

    /**
     * Write an attribute
     *
     * @param writer
     * @param attributeName QName of the attribute
     * @param attributeValue
     * @throws ProcessingException
     */
    public static void writeAttribute(XMLStreamWriter writer, QName attributeName, String attributeValue)
            throws ProcessingException {
        try {
            writer.writeAttribute(attributeName.getPrefix(), attributeName.getNamespaceURI(), attributeName.getLocalPart(),
                    attributeValue);
        } catch (XMLStreamException e) {
            throw logger.processingError(e);
        }
    }

    /**
     * Write an xml attribute
     *
     * @param writer
     * @param localName localpart
     * @param value value of the attribute
     * @throws ProcessingException
     */
    public static void writeAttribute(XMLStreamWriter writer, String localName, String value) throws ProcessingException {
        try {
            writer.writeAttribute(localName, value);
        } catch (XMLStreamException e) {
            throw logger.processingError(e);
        }
    }

    /**
     * Write an xml attribute
     *
     * @param writer
     * @param localName localpart
     * @param type typically xsi:type
     * @param value value of the attribute
     * @throws ProcessingException
     */
    public static void writeAttribute(XMLStreamWriter writer, String localName, String type, String value)
            throws ProcessingException {
        try {
            writer.writeAttribute(localName, type, value);
        } catch (XMLStreamException e) {
            throw logger.processingError(e);
        }
    }

    /**
     * Write an xml attribute
     *
     * @param writer
     * @param prefix prefix for the attribute
     * @param localName localpart
     * @param type typically xsi:type
     * @param value value of the attribute
     * @throws ProcessingException
     */
    public static void writeAttribute(XMLStreamWriter writer, String prefix, String localName, String type, String value)
            throws ProcessingException {
        try {
            writer.writeAttribute(prefix, localName, type, value);
        } catch (XMLStreamException e) {
            throw logger.processingError(e);
        }
    }

    /**
     * Write a string as text node
     *
     * @param writer
     * @param value
     * @throws ProcessingException
     */
    public static void writeCharacters(XMLStreamWriter writer, String value) throws ProcessingException {
        try {
            writer.writeCharacters(value);
        } catch (XMLStreamException e) {
            throw logger.processingError(e);
        }
    }

    /**
     * Write a string as text node
     *
     * @param writer
     * @param value
     * @throws ProcessingException
     */
    public static void writeCData(XMLStreamWriter writer, String value) throws ProcessingException {
        try {
            writer.writeCData(value);
        } catch (XMLStreamException e) {
            throw logger.processingError(e);
        }
    }

    /**
     * Write the default namespace
     *
     * @param writer
     * @param ns
     * @throws ProcessingException
     */
    public static void writeDefaultNameSpace(XMLStreamWriter writer, String ns) throws ProcessingException {
        try {
            writer.writeDefaultNamespace(ns);
        } catch (XMLStreamException e) {
            throw logger.processingError(e);
        }
    }

    /**
     * Write a DOM Node to the stream
     *
     * @param writer
     * @param node
     * @throws ProcessingException
     */
    public static void writeDOMNode(XMLStreamWriter writer, Node node) throws ProcessingException {
        try {
            short nodeType = node.getNodeType();

            switch (nodeType) {
                case Node.ELEMENT_NODE:
                    writeDOMElement(writer, (Element) node);
                    break;
                case Node.TEXT_NODE:
                    writer.writeCharacters(node.getNodeValue());
                    break;
                case Node.COMMENT_NODE:
                    writer.writeComment(node.getNodeValue());
                    break;
                case Node.CDATA_SECTION_NODE:
                    writer.writeCData(node.getNodeValue());
                    break;
                default:
                    // Don't care
            }
        } catch (DOMException e) {
            throw logger.processingError(e);
        } catch (XMLStreamException e) {
            throw logger.processingError(e);
        }
    }

    /**
     * Write DOM Element to the stream
     *
     * @param writer
     * @param domElement
     * @throws ProcessingException
     */
    public static void writeDOMElement(XMLStreamWriter writer, Element domElement) throws ProcessingException {
        if (registeredNSStack.get() == null) {
            registeredNSStack.set(new Stack<String>());
        }
        String domElementPrefix = domElement.getPrefix();

        if (domElementPrefix == null) {
            domElementPrefix = "";
        }

        String domElementNS = domElement.getNamespaceURI();
        if (domElementNS == null) {
            domElementNS = "";
        }

        writeStartElement(writer, domElementPrefix, domElement.getLocalName(), domElementNS);

        // Should we register namespace
        if (domElementPrefix != "" && !registeredNSStack.get().contains(domElementNS)) {
            // writeNameSpace(writer, domElementPrefix, domElementNS );
            registeredNSStack.get().push(domElementNS);
        } else if (domElementPrefix == "" && domElementNS != null) {
            writeNameSpace(writer, "xmlns", domElementNS);
        }

        // Deal with Attributes
        NamedNodeMap attrs = domElement.getAttributes();
        for (int i = 0, len = attrs.getLength(); i < len; ++i) {
            Attr attr = (Attr) attrs.item(i);
            String attributePrefix = attr.getPrefix();
            String attribLocalName = attr.getLocalName();
            String attribValue = attr.getValue();

            if (attributePrefix == null || attributePrefix.length() == 0) {
                if (!("xmlns".equals(attribLocalName))) {
                    writeAttribute(writer, attribLocalName, attribValue);
                }
            } else {
                if ("xmlns".equals(attributePrefix)) {
                    writeNameSpace(writer, attribLocalName, attribValue);
                } else {
                    writeAttribute(writer, new QName(attr.getNamespaceURI(), attribLocalName, attributePrefix), attribValue);
                }
            }
        }

        for (Node child = domElement.getFirstChild(); child != null; child = child.getNextSibling()) {
            writeDOMNode(writer, child);
        }

        writeEndElement(writer);
    }

    /**
     * Write a namespace
     *
     * @param writer
     * @param prefix prefix
     * @param ns Namespace URI
     * @throws ProcessingException
     */
    public static void writeNameSpace(XMLStreamWriter writer, String prefix, String ns) throws ProcessingException {
        try {
            writer.writeNamespace(prefix, ns);
        } catch (XMLStreamException e) {
            throw logger.processingError(e);
        }
    }

    /**
     * Write a start element
     *
     * @param writer
     * @param prefix
     * @param localPart
     * @param ns
     * @throws ProcessingException
     */
    public static void writeStartElement(XMLStreamWriter writer, String prefix, String localPart, String ns)
            throws ProcessingException {
        try {
            writer.writeStartElement(prefix, localPart, ns);
        } catch (XMLStreamException e) {
            throw logger.processingError(e);
        }
    }

    /**
     * <p>
     * Write an end element. The stream writer keeps track of which start element needs to be closed with an end tag.
     * </p>
     *
     * @param writer
     * @throws ProcessingException
     */
    public static void writeEndElement(XMLStreamWriter writer) throws ProcessingException {
        try {
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw logger.processingError(e);
        }
    }

    /**
     * Write the {@link KeyInfoType}
     * @param writer
     * @param keyInfo
     * @throws ProcessingException
     */
    public static void writeKeyInfo(XMLStreamWriter writer, KeyInfoType keyInfo) throws ProcessingException {
        if (keyInfo.getContent() == null || keyInfo.getContent().size() == 0)
            throw logger.writerInvalidKeyInfoNullContentError();
        StaxUtil.writeStartElement(writer, WSTrustConstants.XMLDSig.DSIG_PREFIX, WSTrustConstants.XMLDSig.KEYINFO,
                WSTrustConstants.XMLDSig.DSIG_NS);
        StaxUtil.writeNameSpace(writer, WSTrustConstants.XMLDSig.DSIG_PREFIX, WSTrustConstants.XMLDSig.DSIG_NS);
        // write the keyInfo content.
        Object content = keyInfo.getContent().get(0);
        if (content instanceof Element) {
            Element element = (Element) keyInfo.getContent().get(0);
            StaxUtil.writeDOMNode(writer, element);
        } else if (content instanceof X509DataType) {
            X509DataType type = (X509DataType) content;
            if (type.getDataObjects().size() == 0)
                throw logger.writerNullValueError("X509Data");
            StaxUtil.writeStartElement(writer, WSTrustConstants.XMLDSig.DSIG_PREFIX, WSTrustConstants.XMLDSig.X509DATA,
                    WSTrustConstants.XMLDSig.DSIG_NS);
            Object obj = type.getDataObjects().get(0);
            if (obj instanceof Element) {
                Element element = (Element) obj;
                StaxUtil.writeDOMElement(writer, element);
            } else if (obj instanceof X509CertificateType) {
                X509CertificateType cert = (X509CertificateType) obj;
                StaxUtil.writeStartElement(writer, WSTrustConstants.XMLDSig.DSIG_PREFIX, WSTrustConstants.XMLDSig.X509CERT,
                        WSTrustConstants.XMLDSig.DSIG_NS);
                StaxUtil.writeCharacters(writer, new String(cert.getEncodedCertificate()));
                StaxUtil.writeEndElement(writer);
            }
            StaxUtil.writeEndElement(writer);
        } else if( content instanceof KeyValueType){
            KeyValueType keyvalueType = (KeyValueType) content;
            StaxUtil.writeStartElement(writer, WSTrustConstants.XMLDSig.DSIG_PREFIX, WSTrustConstants.XMLDSig.KEYVALUE,
                    WSTrustConstants.XMLDSig.DSIG_NS);
            if(keyvalueType instanceof DSAKeyValueType){
                StaxUtil.writeDSAKeyValueType(writer, (DSAKeyValueType) keyvalueType);
            }
            if(keyvalueType instanceof RSAKeyValueType){
                StaxUtil.writeRSAKeyValueType(writer, (RSAKeyValueType) keyvalueType);
            }
            StaxUtil.writeEndElement(writer);
        } else
            throw new ProcessingException(ErrorCodes.UNSUPPORTED_TYPE + content);

        StaxUtil.writeEndElement(writer);
    }
    
    public static void writeRSAKeyValueType(XMLStreamWriter writer, RSAKeyValueType type) throws ProcessingException {
        String prefix = WSTrustConstants.XMLDSig.DSIG_PREFIX;
        
        StaxUtil.writeStartElement(writer, prefix, WSTrustConstants.XMLDSig.RSA_KEYVALUE, WSTrustConstants.DSIG_NS);
        // write the rsa key modulus.
        byte[] modulus = type.getModulus();
        writeStartElement(writer, prefix, WSTrustConstants.XMLDSig.MODULUS, WSTrustConstants.DSIG_NS);
        writeCharacters(writer, new String(modulus));
        writeEndElement(writer);

        // write the rsa key exponent.
        byte[] exponent = type.getExponent();
        writeStartElement(writer, prefix, WSTrustConstants.XMLDSig.EXPONENT, WSTrustConstants.DSIG_NS);
        writeCharacters(writer, new String(exponent));
        writeEndElement(writer);

        writeEndElement(writer);
    }
    
    public static void writeDSAKeyValueType(XMLStreamWriter writer, DSAKeyValueType type) throws ProcessingException {
        
        String prefix = WSTrustConstants.XMLDSig.DSIG_PREFIX;
        
        writeStartElement(writer, prefix, WSTrustConstants.XMLDSig.DSA_KEYVALUE, WSTrustConstants.DSIG_NS);
        
        byte[] p = type.getP();
        if(p != null){
            writeStartElement(writer, prefix, WSTrustConstants.XMLDSig.P, WSTrustConstants.DSIG_NS);
            writeCharacters(writer, new String(p));
            writeEndElement(writer);    
        }
        byte[] q = type.getQ();
        if(q != null){
            writeStartElement(writer, prefix, WSTrustConstants.XMLDSig.Q, WSTrustConstants.DSIG_NS);
            writeCharacters(writer, new String(q));
            writeEndElement(writer);    
        }
        byte[] g = type.getG();
        if(g != null){
            writeStartElement(writer, prefix, WSTrustConstants.XMLDSig.G, WSTrustConstants.DSIG_NS);
            writeCharacters(writer, new String(g));
            writeEndElement(writer);    
        }
        byte[] y = type.getY();
        if(y != null){
            writeStartElement(writer, prefix, WSTrustConstants.XMLDSig.Y, WSTrustConstants.DSIG_NS);
            writeCharacters(writer, new String(y));
            writeEndElement(writer);    
        }
        byte[] seed = type.getSeed();
        if(seed != null){
            writeStartElement(writer, prefix, WSTrustConstants.XMLDSig.SEED, WSTrustConstants.DSIG_NS);
            writeCharacters(writer, new String(seed));
            writeEndElement(writer);    
        }
        byte[] pgen = type.getPgenCounter();
        if(pgen != null){
            writeStartElement(writer, prefix, WSTrustConstants.XMLDSig.PGEN_COUNTER, WSTrustConstants.DSIG_NS);
            writeCharacters(writer, new String(pgen));
            writeEndElement(writer);    
        }
        
        writeEndElement(writer);
    }
}