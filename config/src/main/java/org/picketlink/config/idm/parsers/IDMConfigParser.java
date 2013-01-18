/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.picketlink.config.idm.parsers;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.picketlink.identity.federation.core.config.idm.IDMType;
import org.picketlink.identity.federation.core.config.idm.IdentityConfigurationType;
import org.picketlink.identity.federation.core.config.idm.IdentityStoreInvocationContextFactoryType;
import org.picketlink.identity.federation.core.config.idm.StoreConfigurationType;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.handler.config.Handler;
import org.picketlink.identity.federation.core.parsers.AbstractParser;
import org.picketlink.identity.federation.core.parsers.util.StaxParserUtil;
import org.picketlink.identity.federation.core.util.StringUtil;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class IDMConfigParser extends AbstractParser {

    // Root element
    public static final String ROOT_ELEMENT = "PicketLinkIDM";

    // Subelements of PicketlinkIDM element
    public static final String IDENTITY_MANAGER_ELEMENT = "IdentityManager";

    public static final String STORE_FACTORY_ELEMENT = "StoreFactory";

    public static final String IDENTITY_STORE_INVOCATION_CONTEXT_FACTORY_ELEMENT = "IdentityStoreInvocationContextFactory";

    public static final String IDENTITY_CONFIGURATION_ELEMENT = "IdentityConfiguration";

    // ClassName attribute
    public static final String CLASS_NAME_ATTRIBUTE = "ClassName";

    // Subelements of IdentityStoreInvocationContextFactory element
    public static final String ENTITY_MANAGER_FACTORY_ELEMENT = "EntityManagerFactory";

    public static final String EVENT_BRIDGE_ELEMENT = "EventBridge";

    public static final String CREDENTIAL_HANDLER_FACTORY_ELEMENT = "CredentialHandlerFactory";

    public static final String IDENTITY_CACHE_ELEMENT = "IdentityCache";

    public static final String ID_GENERATOR_ELEMENT = "IdGenerator";

    // Subelements of IdentityConfiguration element
    public static final String IDENTITY_STORE_CONFIGURATION_ELEMENT = "IdentityStoreConfiguration";

    public static final String PARTITION_STORE_CONFIGURATION_ELEMENT = "PartitionStoreConfiguration";

    // Property element and property name attribute
    public static final String PROPERTY_ELEMENT = "Property";

    public static final String PROPERTY_NAME_ATTRIBUTE = "Name";

    public Object parse(XMLEventReader xmlEventReader) throws ParsingException {
        StartElement startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
        StaxParserUtil.validate(startElement, ROOT_ELEMENT);
        IDMType idmType = new IDMType();

        QName classNameAttributeQName = new QName("", CLASS_NAME_ATTRIBUTE);

        // parse the inner elements
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = StaxParserUtil.peek(xmlEventReader);
            if (xmlEvent == null)
                break;
            if (xmlEvent instanceof EndElement) {
                EndElement endElement = (EndElement) StaxParserUtil.getNextEvent(xmlEventReader);
                String endElementName = StaxParserUtil.getEndElementName(endElement);
                if (endElementName.equals(ROOT_ELEMENT))
                    break;
            }

            startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
            if (startElement == null)
                break;
            String elementName = StaxParserUtil.getStartElementName(startElement);
            if (elementName.equals(IDENTITY_MANAGER_ELEMENT)) {
                Attribute attribute = startElement.getAttributeByName(classNameAttributeQName);
                if (attribute != null)
                    idmType.setIdentityManagerClass(StaxParserUtil.getAttributeValue(attribute));
            } else if (elementName.equals(STORE_FACTORY_ELEMENT)) {
                Attribute attribute = startElement.getAttributeByName(classNameAttributeQName);
                if (attribute != null)
                    idmType.setStoreFactoryClass(StaxParserUtil.getAttributeValue(attribute));
            } else if (elementName.equals(IDENTITY_STORE_INVOCATION_CONTEXT_FACTORY_ELEMENT)) {
                IdentityStoreInvocationContextFactoryType invContextFactoryType = parseIdentityStoreInvocationContextFactoryConfiguration(xmlEventReader, startElement);
                idmType.setIdentityStoreInvocationContextFactory(invContextFactoryType);
            } else if (elementName.equals(IDENTITY_CONFIGURATION_ELEMENT)) {
                IdentityConfigurationType identityConfiguration = parseIdentityConfiguration(xmlEventReader, startElement);
                idmType.setIdentityConfigurationType(identityConfiguration);
            } else {
                throw logger.parserUnknownStartElement(elementName, startElement.getLocation());
            }
        }

        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = StaxParserUtil.peek(xmlEventReader);
            if (xmlEvent == null)
                break;
            if (xmlEvent instanceof EndElement) {
                EndElement endElement = (EndElement) StaxParserUtil.getNextEvent(xmlEventReader);
                String endElementName = StaxParserUtil.getEndElementName(endElement);
                if (endElementName.equals(ROOT_ELEMENT))
                    break;
//                else
//                    throw logger.parserUnknownEndElement(endElementName);
            }
            else {
                // TODO: temporary
                StaxParserUtil.getNextEvent(xmlEventReader);
            }
        }
        return idmType;
    }

    public boolean supports(QName qname) {
        return false;
    }

    protected IdentityStoreInvocationContextFactoryType parseIdentityStoreInvocationContextFactoryConfiguration(XMLEventReader xmlEventReader,
            StartElement startElement) throws ParsingException {
        QName classNameAttributeQName = new QName("", CLASS_NAME_ATTRIBUTE);

        IdentityStoreInvocationContextFactoryType invContextFactoryType = new IdentityStoreInvocationContextFactoryType();

        Attribute attribute = startElement.getAttributeByName(classNameAttributeQName);
        if (attribute != null)
            invContextFactoryType.setClassName(StaxParserUtil.getAttributeValue(attribute));

        // parse the inner elements
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = StaxParserUtil.peek(xmlEventReader);
            if (xmlEvent == null)
                break;
            if (xmlEvent instanceof EndElement) {
                EndElement endElement = (EndElement) StaxParserUtil.getNextEvent(xmlEventReader);
                String endElementName = StaxParserUtil.getEndElementName(endElement);
                if (endElementName.equals(IDENTITY_STORE_INVOCATION_CONTEXT_FACTORY_ELEMENT))
                    break;
                else
                    continue;
            }

            startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
            if (startElement == null)
                break;
            String elementName = StaxParserUtil.getStartElementName(startElement);

            if (elementName.equals(ENTITY_MANAGER_FACTORY_ELEMENT)) {
                attribute = startElement.getAttributeByName(classNameAttributeQName);
                if (attribute != null)
                    invContextFactoryType.setEntityManagerFactoryClass(StaxParserUtil.getAttributeValue(attribute));
            } else if (elementName.equals(EVENT_BRIDGE_ELEMENT)) {
                attribute = startElement.getAttributeByName(classNameAttributeQName);
                if (attribute != null)
                    invContextFactoryType.setEventBridgeClass(StaxParserUtil.getAttributeValue(attribute));
            } else if (elementName.equals(CREDENTIAL_HANDLER_FACTORY_ELEMENT)) {
                attribute = startElement.getAttributeByName(classNameAttributeQName);
                if (attribute != null)
                    invContextFactoryType.setCredentialHandlerFactoryClass(StaxParserUtil.getAttributeValue(attribute));
            } else if (elementName.equals(IDENTITY_CACHE_ELEMENT)) {
                attribute = startElement.getAttributeByName(classNameAttributeQName);
                if (attribute != null)
                    invContextFactoryType.setIdentityCacheClass(StaxParserUtil.getAttributeValue(attribute));
            } else if (elementName.equals(ID_GENERATOR_ELEMENT)) {
                attribute = startElement.getAttributeByName(classNameAttributeQName);
                if (attribute != null)
                    invContextFactoryType.setIdGeneratorClass(StaxParserUtil.getAttributeValue(attribute));
            } else {
                throw logger.parserUnknownStartElement(elementName, startElement.getLocation());
            }
        }

        return invContextFactoryType;
    }

    protected IdentityConfigurationType parseIdentityConfiguration(XMLEventReader xmlEventReader, StartElement startElement)
            throws ParsingException {
        IdentityConfigurationType identityConfigurationType = new IdentityConfigurationType();

        // parse the inner elements
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = StaxParserUtil.peek(xmlEventReader);
            if (xmlEvent == null)
                break;
            if (xmlEvent instanceof EndElement) {
                EndElement endElement = (EndElement) StaxParserUtil.getNextEvent(xmlEventReader);
                String endElementName = StaxParserUtil.getEndElementName(endElement);
                if (endElementName.equals(IDENTITY_CONFIGURATION_ELEMENT))
                    break;
                else
                    throw logger.parserUnknownEndElement(endElementName);
            }

            startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
            if (startElement == null)
                break;
            String elementName = StaxParserUtil.getStartElementName(startElement);

            if (elementName.equals(IDENTITY_STORE_CONFIGURATION_ELEMENT)) {
                StoreConfigurationType identityStoreConfiguration = parseStoreConfiguration(xmlEventReader, startElement);
                identityConfigurationType.addIdentityStoreConfiguration(identityStoreConfiguration);
            } else if (elementName.equals(PARTITION_STORE_CONFIGURATION_ELEMENT)) {
                StoreConfigurationType partitionStoreConfiguration = parseStoreConfiguration(xmlEventReader, startElement);
                identityConfigurationType.setPartitionStoreConfiguration(partitionStoreConfiguration);
            } else {
                throw logger.parserUnknownStartElement(elementName, startElement.getLocation());
            }
        }

        return identityConfigurationType;
    }

    protected StoreConfigurationType parseStoreConfiguration(XMLEventReader xmlEventReader, StartElement startElement)
            throws ParsingException {
        QName classNameAttributeQName = new QName("", CLASS_NAME_ATTRIBUTE);
        String storeFactoryElementName = StaxParserUtil.getStartElementName(startElement);

        StoreConfigurationType storeConfigurationType = new StoreConfigurationType();

        Attribute attribute = startElement.getAttributeByName(classNameAttributeQName);
        if (attribute != null)
            storeConfigurationType.setClassName(StaxParserUtil.getAttributeValue(attribute));

        // parse the inner elements
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = StaxParserUtil.peek(xmlEventReader);
            if (xmlEvent == null)
                break;
            if (xmlEvent instanceof EndElement) {
                EndElement endElement = (EndElement) StaxParserUtil.getNextEvent(xmlEventReader);
                String endElementName = StaxParserUtil.getEndElementName(endElement);
                if (endElementName.equals(storeFactoryElementName))
                    break;
                else
                    continue;
            }

            startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
            if (startElement == null)
                break;
            String elementName = StaxParserUtil.getStartElementName(startElement);

            if (elementName.equals(PROPERTY_ELEMENT)) {
                // parsing property name
                String propertyName = null;
                QName attributeQName = new QName("", PROPERTY_NAME_ATTRIBUTE);
                attribute = startElement.getAttributeByName(attributeQName);
                if (attribute != null)
                    propertyName = StaxParserUtil.getAttributeValue(attribute);

                // parsing property value (for now we assume characters only)
                String propertyValue = null;
                while (true) {
                    xmlEvent = StaxParserUtil.getNextEvent(xmlEventReader);
                    if (xmlEvent.isCharacters()) {
                        Characters chars = xmlEvent.asCharacters();
                        propertyValue = chars.getData();
                        if (propertyValue != null) {
                            propertyValue = StringUtil.getSystemPropertyAsString(propertyValue.trim());
                        }
                        break;
                    } else if (xmlEvent instanceof EndElement) {
                        EndElement endElement = (EndElement) xmlEvent;
                        String endElementName = StaxParserUtil.getEndElementName(endElement);
                        if (endElementName.equals(PROPERTY_ELEMENT))
                            break;
                    }
                }

                storeConfigurationType.addProperty(propertyName, propertyValue);
            } else {
                throw logger.parserUnknownStartElement(elementName, startElement.getLocation());
            }
        }

        return storeConfigurationType;
    }


}
