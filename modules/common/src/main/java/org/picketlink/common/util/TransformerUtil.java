/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.picketlink.common.util;

import java.util.Iterator;
import java.util.Properties;
import java.util.Stack;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.util.JAXBSource;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stax.StAXSource;

import org.picketlink.common.ErrorCodes;
import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.constants.GeneralConstants;
import org.picketlink.common.exceptions.ConfigurationException;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.common.exceptions.ProcessingException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Utility to deal with JAXP Transformer
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 22, 2010
 */
public class TransformerUtil {
    
    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();
    
    private static TransformerFactory transformerFactory;
    
    /**
     * Get the Default Transformer
     *
     * @return
     * @throws ConfigurationException
     */
    public static Transformer getTransformer() throws ConfigurationException {
        Transformer transformer;
        try {
            transformer = getTransformerFactory().newTransformer();
        } catch (TransformerConfigurationException e) {
            throw logger.configurationError(e);
        } catch (TransformerFactoryConfigurationError e) {
            throw logger.configurationError(e);
        }
        
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
        
        return transformer;
    }

    /**
     * <p>Creates a {@link TransformerFactory}. The returned instance is cached and shared between different threads.</p>
     *
     * @return
     * @throws TransformerFactoryConfigurationError
     */
    public static TransformerFactory getTransformerFactory() throws TransformerFactoryConfigurationError {
        if (transformerFactory == null) {
            boolean tccl_jaxp = SystemPropertiesUtil.getSystemProperty(GeneralConstants.TCCL_JAXP, "false")
                    .equalsIgnoreCase("true");
            ClassLoader prevTCCL = SecurityActions.getTCCL();
            try{
                if(tccl_jaxp){
                    SecurityActions.setTCCL(TransformerUtil.class.getClassLoader());
                }
                transformerFactory = TransformerFactory.newInstance();
            } finally {
                if(tccl_jaxp){
                    SecurityActions.setTCCL(prevTCCL);
                }
            }
        }

        return transformerFactory;
    }

    /**
     * Get the Custom Stax Source to DOM result transformer that has been written to get over the JDK transformer bugs (JDK6) as
     * well as the issue of Xalan installing its Transformer (which does not support stax).
     *
     * @return
     * @throws ConfigurationException
     */
    public static Transformer getStaxSourceToDomResultTransformer() throws ConfigurationException {
        return new PicketLinkStaxToDOMTransformer();
    }

    /**
     * Use the transformer to transform
     *
     * @param transformer
     * @param stax
     * @param result
     * @throws ParsingException
     */
    public static void transform(Transformer transformer, StAXSource stax, DOMResult result) throws ParsingException {
        transform(transformer, (Source)stax, result);
    }

    /**
     * Use the transformer to transform
     *
     * @param transformer
     * @param source
     * @param result
     * @throws ParsingException
     */
    public static void transform(Transformer transformer, Source source, DOMResult result) throws ParsingException {
        boolean tccl_jaxp = SystemPropertiesUtil.getSystemProperty(GeneralConstants.TCCL_JAXP,"false").equalsIgnoreCase("true");
        ClassLoader prevCL = SecurityActions.getTCCL();
        try {
            if(tccl_jaxp){
                SecurityActions.setTCCL(TransformerUtil.class.getClassLoader());
            }
            transformer.transform(source, result);
        } catch (TransformerException e) {
            throw logger.parserError(e);
        }finally{
            if(tccl_jaxp){
                SecurityActions.setTCCL(prevCL);
            }
        }
    }

    public static void transform(JAXBContext context, JAXBElement<?> jaxb, Result result) throws ParsingException {
        try {
            Transformer transformer = getTransformer();
            JAXBSource jaxbSource = new JAXBSource(context, jaxb);

            transformer.transform(jaxbSource, result);
        } catch (Exception e) {
            throw logger.parserError(e);
        }
    }

    /**
     * Custom Project {@code Transformer} that can take in a {@link StAXSource} and transform into {@link DOMResult}
     *
     * @author anil
     */
    private static class PicketLinkStaxToDOMTransformer extends Transformer {
        @Override
        public void transform(Source xmlSource, Result outputTarget) throws TransformerException {
            if (!(xmlSource instanceof StAXSource))
                throw logger.wrongTypeError("xmlSource should be a stax source");
            if (outputTarget instanceof DOMResult == false)
                throw logger.wrongTypeError("outputTarget should be a dom result");

            String rootTag = null;

            StAXSource staxSource = (StAXSource) xmlSource;
            XMLEventReader xmlEventReader = staxSource.getXMLEventReader();
            if (xmlEventReader == null)
                throw new TransformerException(logger.nullValueError("XMLEventReader"));

            DOMResult domResult = (DOMResult) outputTarget;
            Document doc = (Document) domResult.getNode();

            Stack<Node> stack = new Stack<Node>();

            try {
                XMLEvent xmlEvent = StaxParserUtil.getNextEvent(xmlEventReader);
                if (xmlEvent instanceof StartElement == false)
                    throw new TransformerException(ErrorCodes.WRITER_SHOULD_START_ELEMENT);

                StartElement rootElement = (StartElement) xmlEvent;
                rootTag = StaxParserUtil.getStartElementName(rootElement);
                Element docRoot = handleStartElement(xmlEventReader, rootElement, new CustomHolder(doc, false));
                Node parent = doc.importNode(docRoot, true);
                doc.appendChild(parent);

                stack.push(parent);

                while (xmlEventReader.hasNext()) {
                    xmlEvent = StaxParserUtil.getNextEvent(xmlEventReader);
                    int type = xmlEvent.getEventType();
                    switch (type) {
                        case XMLEvent.START_ELEMENT:
                            StartElement startElement = (StartElement) xmlEvent;
                            CustomHolder holder = new CustomHolder(doc, false);
                            Element docStartElement = handleStartElement(xmlEventReader, startElement, holder);
                            Node el = doc.importNode(docStartElement, true);

                            Node top = null;

                            if (!stack.isEmpty()) {
                                top = stack.peek();
                            }

                            if (!holder.encounteredTextNode) {
                                stack.push(el);
                            }

                            if (top == null)
                                doc.appendChild(el);
                            else
                                top.appendChild(el);
                            break;
                        case XMLEvent.END_ELEMENT:
                            EndElement endElement = (EndElement) xmlEvent;
                            String endTag = StaxParserUtil.getEndElementName(endElement);
                            if (rootTag.equals(endTag))
                                return; // We are done with the dom parsing
                            else {
                                if (!stack.isEmpty())
                                    stack.pop();
                            }
                            break;
                    }
                }
            } catch (Exception e) {
                throw new TransformerException(e);
            }
        }

        @Override
        public void setParameter(String name, Object value) {
        }

        @Override
        public Object getParameter(String name) {
            return null;
        }

        @Override
        public void clearParameters() {
        }

        @Override
        public void setURIResolver(URIResolver resolver) {
        }

        @Override
        public URIResolver getURIResolver() {
            return null;
        }

        @Override
        public void setOutputProperties(Properties oformat) {
        }

        @Override
        public Properties getOutputProperties() {
            return null;
        }

        @Override
        public void setOutputProperty(String name, String value) throws IllegalArgumentException {
        }

        @Override
        public String getOutputProperty(String name) throws IllegalArgumentException {
            return null;
        }

        @Override
        public void setErrorListener(ErrorListener listener) throws IllegalArgumentException {
        }

        @Override
        public ErrorListener getErrorListener() {
            return null;
        }

        private Element handleStartElement(XMLEventReader xmlEventReader, StartElement startElement, CustomHolder holder)
                throws ParsingException, ProcessingException {
            Document doc = holder.doc;

            QName elementName = startElement.getName();
            String ns = elementName.getNamespaceURI();
            String prefix = elementName.getPrefix();
            String localPart = elementName.getLocalPart();

            String qual = prefix != null && prefix != "" ? prefix + ":" + localPart : localPart;

            Element el = doc.createElementNS(ns, qual);

            String containsBaseNamespace = containsBaseNamespace(startElement);
            if (StringUtil.isNotNull(containsBaseNamespace)) {
                el = DocumentUtil.createDocumentWithBaseNamespace(containsBaseNamespace, localPart).getDocumentElement();
                el = (Element) doc.importNode(el, true);
            }
            if (StringUtil.isNotNull(prefix)) {
                el.setPrefix(prefix);
            }

            // Look for attributes
            @SuppressWarnings("unchecked")
            Iterator<Attribute> attrs = startElement.getAttributes();
            while (attrs != null && attrs.hasNext()) {
                Attribute attr = attrs.next();
                QName attrName = attr.getName();
                ns = attrName.getNamespaceURI();
                prefix = attrName.getPrefix();
                localPart = attrName.getLocalPart();
                qual = prefix != null && prefix != "" ? prefix + ":" + localPart : localPart;

                if (logger.isTraceEnabled()) {
                    logger.trace("Creating an Attribute Namespace=" + ns + ":" + qual);
                }
                doc.createAttributeNS(ns, qual);
                el.setAttributeNS(ns, qual, attr.getValue());
            }

            // look for namespaces
            @SuppressWarnings("unchecked")
            Iterator<Namespace> namespaces = startElement.getNamespaces();
            while (namespaces != null && namespaces.hasNext()) {
                Namespace namespace = namespaces.next();
                QName name = namespace.getName();
                localPart = name.getLocalPart();
                prefix = name.getPrefix();
                if (prefix != null && prefix != "")
                    qual = (localPart != null && localPart != "") ? prefix + ":" + localPart : prefix;

                if (qual.equals("xmlns"))
                    continue;
                if (logger.isTraceEnabled()) {
                    logger.trace("Set Attribute Namespace=" + name.getNamespaceURI() + "::Qual=:" + qual + "::Value="
                            + namespace.getNamespaceURI());
                }
                if (qual != null && qual.startsWith("xmlns")) {
                    el.setAttributeNS(name.getNamespaceURI(), qual, namespace.getNamespaceURI());
                }
            }

            XMLEvent nextEvent = StaxParserUtil.peek(xmlEventReader);
            if (nextEvent instanceof Comment) {
                Comment commentEvent = (Comment) nextEvent;
                Node commentNode = doc.createComment(commentEvent.getText());
                commentNode = doc.importNode(commentNode, true);
                el.appendChild(commentNode);
            } else if (nextEvent.getEventType() == XMLEvent.CHARACTERS) {
                Characters characterEvent = (Characters) nextEvent;
                String trimmedData = characterEvent.getData().trim();

                if (trimmedData != null && trimmedData.length() > 0) {
                    holder.encounteredTextNode = true;
                    try {
                        String text = StaxParserUtil.getElementText(xmlEventReader);

                        Node textNode = doc.createTextNode(text);
                        textNode = doc.importNode(textNode, true);
                        el.appendChild(textNode);
                    } catch (Exception e) {
                        throw logger.parserException(e);
                    }
                }
            }
            return el;
        }

        @SuppressWarnings("unchecked")
        private String containsBaseNamespace(StartElement startElement) {
            String localPart, prefix, qual = null;

            Iterator<Namespace> namespaces = startElement.getNamespaces();
            while (namespaces != null && namespaces.hasNext()) {
                Namespace namespace = namespaces.next();
                QName name = namespace.getName();
                localPart = name.getLocalPart();
                prefix = name.getPrefix();
                if (prefix != null && prefix != "")
                    qual = (localPart != null && localPart != "") ? prefix + ":" + localPart : prefix;

                if (qual != null && qual.equals("xmlns"))
                    return namespace.getNamespaceURI();
            }
            return null;
        }

        private class CustomHolder {
            public Document doc;

            public boolean encounteredTextNode = false;

            public CustomHolder(Document document, boolean bool) {
                this.doc = document;
                this.encounteredTextNode = bool;
            }
        }
    }
}