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
package org.picketlink.config.federation.parsers;

import org.picketlink.common.ErrorCodes;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.common.parsers.AbstractParser;
import org.picketlink.common.util.StaxParserUtil;
import org.picketlink.config.federation.AuthPropertyType;
import org.picketlink.config.federation.ClaimsProcessorType;
import org.picketlink.config.federation.ClaimsProcessorsType;
import org.picketlink.config.federation.KeyProviderType;
import org.picketlink.config.federation.KeyValueType;
import org.picketlink.config.federation.STSType;
import org.picketlink.config.federation.ServiceProviderType;
import org.picketlink.config.federation.ServiceProvidersType;
import org.picketlink.config.federation.TokenProviderType;
import org.picketlink.config.federation.TokenProvidersType;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * <p>
 * A Stax parser for the STS configuration.
 * </p>
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class STSConfigParser extends AbstractParser {

    private static final String CONFIG_NS = "urn:picketlink:identity-federation:config:1.0";

    // XML configuration elements.
    public static final String ROOT_ELEMENT = "PicketLinkSTS";

    private static final String KEY_PROVIDER_ELEMENT = "KeyProvider";

    private static final String AUTH_ELEMENT = "Auth";

    private static final String SIGNING_ALIAS_ELEMENT = "SigningAlias";

    private static final String VALIDATING_ALIAS_ELEMENT = "ValidatingAlias";

    private static final String REQUEST_HANDLER_ELEMENT = "RequestHandler";

    private static final String PROPERTY_ELEMENT = "Property";

    private static final String CLAIMS_PROCESSORS_ELEMENT = "ClaimsProcessors";

    private static final String CLAIMS_PROCESSOR_ELEMENT = "ClaimsProcessor";

    private static final String TOKEN_PROVIDERS_ELEMENT = "TokenProviders";

    private static final String TOKEN_PROVIDER_ELEMENT = "TokenProvider";

    private static final String SERVICE_PROVIDERS_ELEMENT = "ServiceProviders";

    private static final String SERVICE_PROVIDER_ELEMENT = "ServiceProvider";

    // XML configuration attributes.
    private static final String STS_NAME_ATTRIB = "STSName";

    private static final String TOKEN_TIMEOUT_ATTRIB = "TokenTimeout";
    private static final String CLOCK_SKEW_ATTRIB = "ClockSkew";

    private static final String SIGN_TOKEN_ATTRIB = "SignToken";

    private static final String ENCRYPT_TOKEN_ATTRIB = "EncryptToken";

    private static final String CANON_METHOD_ATTRIB = "CanonicalizationMethod";

    private static final String CLASS_NAME_ATTRIB = "ClassName";

    private static final String KEY_ATTRIB = "Key";

    private static final String VALUE_ATTRIB = "Value";

    private static final String DIALECT_ATTRIB = "Dialect";

    private static final String PROCESSOR_CLASS_ATTRIB = "ProcessorClass";

    private static final String PROVIDER_CLASS_ATTRIB = "ProviderClass";

    private static final String TOKEN_TYPE_ATTRIB = "TokenType";

    private static final String TOKEN_ELEMENT_ATTRIB = "TokenElement";

    private static final String TOKEN_ELEMENT_NS_ATTRIB = "TokenElementNS";

    private static final String ENDPOINT_ATTRIB = "Endpoint";

    private static final String ENDPOINT_REGEX_ATTRIB = "EndpointRegEx";

    private static final String TRUSTSTORE_ALIAS_ATTRIB = "TruststoreAlias";

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.identity.federation.core.parsers.ParserNamespaceSupport#parse(javax.xml.stream.XMLEventReader)
     */
    public Object parse(XMLEventReader xmlEventReader) throws ParsingException {
        StartElement startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
        StaxParserUtil.validate(startElement, ROOT_ELEMENT);
        STSType configType = new STSType();

        // parse and set the root element attributes.
        QName attributeQName = new QName("", STS_NAME_ATTRIB);
        Attribute attribute = startElement.getAttributeByName(attributeQName);
        if (attribute != null)
            configType.setSTSName(StaxParserUtil.getAttributeValue(attribute));

        attributeQName = new QName("", TOKEN_TIMEOUT_ATTRIB);
        attribute = startElement.getAttributeByName(attributeQName);
        if (attribute != null)
            configType.setTokenTimeout(Integer.valueOf(StaxParserUtil.getAttributeValue(attribute)));

        attributeQName = new QName("", CLOCK_SKEW_ATTRIB);
        attribute = startElement.getAttributeByName(attributeQName);
        if (attribute != null)
            configType.setClockSkew(Integer.valueOf(StaxParserUtil.getAttributeValue(attribute)));

        attributeQName = new QName("", SIGN_TOKEN_ATTRIB);
        attribute = startElement.getAttributeByName(attributeQName);
        if (attribute != null)
            configType.setSignToken(Boolean.valueOf(StaxParserUtil.getAttributeValue(attribute)));

        attributeQName = new QName("", ENCRYPT_TOKEN_ATTRIB);
        attribute = startElement.getAttributeByName(attributeQName);
        if (attribute != null)
            configType.setEncryptToken(Boolean.valueOf(StaxParserUtil.getAttributeValue(attribute)));

        attributeQName = new QName("", CANON_METHOD_ATTRIB);
        attribute = startElement.getAttributeByName(attributeQName);
        if (attribute != null)
            configType.setCanonicalizationMethod(StaxParserUtil.getAttributeValue(attribute));

        // parse the inner elements.
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = StaxParserUtil.peek(xmlEventReader);
            if (xmlEvent == null)
                break;
            if (xmlEvent instanceof EndElement) {
                EndElement endElement = (EndElement) StaxParserUtil.getNextEvent(xmlEventReader);
                String endElementName = StaxParserUtil.getEndElementName(endElement);
                if (endElementName.equals(ROOT_ELEMENT))
                    break;
                else
                    throw logger.parserUnknownEndElement(endElementName);
            }

            StartElement subEvent = StaxParserUtil.peekNextStartElement(xmlEventReader);
            if (subEvent == null)
                break;
            String elementName = StaxParserUtil.getStartElementName(subEvent);
            if (KEY_PROVIDER_ELEMENT.equalsIgnoreCase(elementName)) {
                configType.setKeyProvider(this.parseKeyProvider(xmlEventReader));
            } else if (REQUEST_HANDLER_ELEMENT.equalsIgnoreCase(elementName)) {
                subEvent = StaxParserUtil.getNextStartElement(xmlEventReader);
                if (!StaxParserUtil.hasTextAhead(xmlEventReader))
                    throw new ParsingException(ErrorCodes.EXPECTED_TEXT_VALUE + "RequestHandler");
                configType.setRequestHandler(StaxParserUtil.getElementText(xmlEventReader));
            } else if (CLAIMS_PROCESSORS_ELEMENT.equalsIgnoreCase(elementName)) {
                configType.setClaimsProcessors(this.parseClaimsProcessors(xmlEventReader));
            } else if (TOKEN_PROVIDERS_ELEMENT.equalsIgnoreCase(elementName)) {
                configType.setTokenProviders(this.parseTokenProviders(xmlEventReader));
            } else if (SERVICE_PROVIDERS_ELEMENT.equalsIgnoreCase(elementName)) {
                configType.setServiceProviders(this.parseServiceProviders(xmlEventReader));
            } else
                throw logger.parserUnknownTag(elementName, subEvent.getLocation());
        }
        return configType;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.identity.federation.core.parsers.ParserNamespaceSupport#supports(javax.xml.namespace.QName)
     */
    public boolean supports(QName qname) {
        return CONFIG_NS.equals(qname.getNamespaceURI());
    }

    /**
     * <p>
     * Parses the {@code KeyProvider} section of the STS configuration file. This section is used to setup the keystore \ that
     * will be used to sign and encrypt security tokens.
     * </p>
     *
     * @param xmlEventReader the reader used to parse the XML configuration file.
     * @return a {@code KeyProviderType} instance that contains the parsed data.
     * @throws ParsingException if an error occurs while parsing the XML file.
     */
    private KeyProviderType parseKeyProvider(XMLEventReader xmlEventReader) throws ParsingException {
        StartElement startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
        StaxParserUtil.validate(startElement, KEY_PROVIDER_ELEMENT);

        KeyProviderType keyProvider = new KeyProviderType();
        // get the key provider class name attribute.
        QName attributeQName = new QName("", CLASS_NAME_ATTRIB);
        Attribute attribute = startElement.getAttributeByName(attributeQName);
        if (attribute == null)
            throw logger.parserRequiredAttribute("ClassName");
        keyProvider.setClassName(StaxParserUtil.getAttributeValue(attribute));

        // parse the inner elements.
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = StaxParserUtil.peek(xmlEventReader);
            if (xmlEvent == null)
                break;
            if (xmlEvent instanceof EndElement) {
                EndElement endElement = (EndElement) StaxParserUtil.getNextEvent(xmlEventReader);
                String endElementName = StaxParserUtil.getEndElementName(endElement);
                if (endElementName.equals(KEY_PROVIDER_ELEMENT))
                    break;
                else
                    throw logger.parserUnknownEndElement(endElementName);
            }

            StartElement subEvent = StaxParserUtil.peekNextStartElement(xmlEventReader);
            if (subEvent == null)
                break;
            String elementName = StaxParserUtil.getStartElementName(subEvent);

            if (SIGNING_ALIAS_ELEMENT.equalsIgnoreCase(elementName)) {
                subEvent = StaxParserUtil.getNextStartElement(xmlEventReader);
                if (!StaxParserUtil.hasTextAhead(xmlEventReader))
                    throw logger.parserExpectedTextValue("SigningAlias");
                keyProvider.setSigningAlias(StaxParserUtil.getElementText(xmlEventReader));
            } else if (VALIDATING_ALIAS_ELEMENT.equalsIgnoreCase(elementName)) {
                subEvent = StaxParserUtil.getNextStartElement(xmlEventReader);
                KeyValueType keyValue = new KeyValueType();
                // parse the key and value attributes.
                attributeQName = new QName("", KEY_ATTRIB);
                attribute = subEvent.getAttributeByName(attributeQName);
                if (attribute != null)
                    keyValue.setKey(StaxParserUtil.getAttributeValue(attribute));
                attributeQName = new QName("", VALUE_ATTRIB);
                attribute = subEvent.getAttributeByName(attributeQName);
                if (attribute != null)
                    keyValue.setValue(StaxParserUtil.getAttributeValue(attribute));

                EndElement endElement = StaxParserUtil.getNextEndElement(xmlEventReader);
                StaxParserUtil.validate(endElement, VALIDATING_ALIAS_ELEMENT);
                keyProvider.add(keyValue);
            } else if (AUTH_ELEMENT.equalsIgnoreCase(elementName)) {
                subEvent = StaxParserUtil.getNextStartElement(xmlEventReader);
                AuthPropertyType authProperty = new AuthPropertyType();
                // parse the key and value attributes.
                attributeQName = new QName("", KEY_ATTRIB);
                attribute = subEvent.getAttributeByName(attributeQName);
                if (attribute != null)
                    authProperty.setKey(StaxParserUtil.getAttributeValue(attribute));
                attributeQName = new QName("", VALUE_ATTRIB);
                attribute = subEvent.getAttributeByName(attributeQName);
                if (attribute != null)
                    authProperty.setValue(StaxParserUtil.getAttributeValue(attribute));

                EndElement endElement = StaxParserUtil.getNextEndElement(xmlEventReader);
                StaxParserUtil.validate(endElement, AUTH_ELEMENT);
                keyProvider.add(authProperty);
            } else
                throw logger.parserUnknownTag(elementName, subEvent.getLocation());
        }
        return keyProvider;
    }

    /**
     * <p>
     * Parses the {@code ClaimsProcessors} section of the STS configuration file.
     * </p>
     *
     * @param xmlEventReader the reader used to parse the XML configuration file.
     * @return a {@code ClaimsProcessorsType} instance that contains the parsed data.
     * @throws ParsingException if an error occurs while parsing the XML file.
     */
    private ClaimsProcessorsType parseClaimsProcessors(XMLEventReader xmlEventReader) throws ParsingException {
        StartElement startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
        StaxParserUtil.validate(startElement, CLAIMS_PROCESSORS_ELEMENT);

        ClaimsProcessorsType claimsProcessors = new ClaimsProcessorsType();

        // parse all claims processors one by one.
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = StaxParserUtil.peek(xmlEventReader);
            if (xmlEvent == null)
                break;
            if (xmlEvent instanceof EndElement) {
                EndElement endElement = (EndElement) StaxParserUtil.getNextEvent(xmlEventReader);
                String endElementName = StaxParserUtil.getEndElementName(endElement);
                if (endElementName.equals(CLAIMS_PROCESSORS_ELEMENT))
                    break;
                else
                    throw logger.parserUnknownEndElement(endElementName);
            }

            StartElement subEvent = StaxParserUtil.peekNextStartElement(xmlEventReader);
            if (subEvent == null)
                break;
            String elementName = StaxParserUtil.getStartElementName(subEvent);

            if (CLAIMS_PROCESSOR_ELEMENT.equalsIgnoreCase(elementName)) {
                subEvent = StaxParserUtil.getNextStartElement(xmlEventReader);
                StaxParserUtil.validate(subEvent, CLAIMS_PROCESSOR_ELEMENT);
                ClaimsProcessorType claimsProcessor = new ClaimsProcessorType();

                // parse the processor attributes (class and dialect).
                QName attributeQName = new QName("", PROCESSOR_CLASS_ATTRIB);
                Attribute attribute = subEvent.getAttributeByName(attributeQName);
                if (attribute != null)
                    claimsProcessor.setProcessorClass(StaxParserUtil.getAttributeValue(attribute));
                attributeQName = new QName("", DIALECT_ATTRIB);
                attribute = subEvent.getAttributeByName(attributeQName);
                if (attribute != null)
                    claimsProcessor.setDialect(StaxParserUtil.getAttributeValue(attribute));

                // parse the processor properties.
                while (xmlEventReader.hasNext()) {
                    xmlEvent = StaxParserUtil.peek(xmlEventReader);
                    if (xmlEvent == null)
                        break;
                    if (xmlEvent instanceof EndElement) {
                        EndElement endElement = (EndElement) StaxParserUtil.getNextEvent(xmlEventReader);
                        String endElementName = StaxParserUtil.getEndElementName(endElement);
                        if (endElementName.equals(CLAIMS_PROCESSOR_ELEMENT))
                            break;
                        else
                            throw logger.parserUnknownEndElement(endElementName);
                    }

                    subEvent = StaxParserUtil.peekNextStartElement(xmlEventReader);
                    if (subEvent == null)
                        break;
                    elementName = StaxParserUtil.getStartElementName(subEvent);
                    if (PROPERTY_ELEMENT.equalsIgnoreCase(elementName)) {
                        // parse the property key and value.
                        subEvent = StaxParserUtil.getNextStartElement(xmlEventReader);
                        KeyValueType keyValue = new KeyValueType();
                        // parse the key and value attributes.
                        attributeQName = new QName("", KEY_ATTRIB);
                        attribute = subEvent.getAttributeByName(attributeQName);
                        if (attribute != null)
                            keyValue.setKey(StaxParserUtil.getAttributeValue(attribute));
                        attributeQName = new QName("", VALUE_ATTRIB);
                        attribute = subEvent.getAttributeByName(attributeQName);
                        if (attribute != null)
                            keyValue.setValue(StaxParserUtil.getAttributeValue(attribute));

                        EndElement endElement = StaxParserUtil.getNextEndElement(xmlEventReader);
                        StaxParserUtil.validate(endElement, PROPERTY_ELEMENT);
                        claimsProcessor.add(keyValue);
                    } else
                        throw logger.parserUnknownTag(elementName, subEvent.getLocation());
                }
                claimsProcessors.add(claimsProcessor);
            } else
                throw logger.parserUnknownTag(elementName, subEvent.getLocation());
        }
        return claimsProcessors;
    }

    /**
     * <p>
     * Parses the {@code TokenProviders} section of the STS configuration file.
     * </p>
     *
     * @param xmlEventReader the reader used to parse the XML configuration file.
     * @return a {@code TokenProvidersType} instance that contains the parsed data.
     * @throws ParsingException if an error occurs while parsing the XML file.
     */
    private TokenProvidersType parseTokenProviders(XMLEventReader xmlEventReader) throws ParsingException {
        StartElement startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
        StaxParserUtil.validate(startElement, TOKEN_PROVIDERS_ELEMENT);

        TokenProvidersType tokenProviders = new TokenProvidersType();

        // parse all token providers one by one.
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = StaxParserUtil.peek(xmlEventReader);
            if (xmlEvent == null)
                break;
            if (xmlEvent instanceof EndElement) {
                EndElement endElement = (EndElement) StaxParserUtil.getNextEvent(xmlEventReader);
                String endElementName = StaxParserUtil.getEndElementName(endElement);
                if (endElementName.equals(TOKEN_PROVIDERS_ELEMENT))
                    break;
                else
                    throw logger.parserUnknownEndElement(endElementName);
            }

            StartElement subEvent = StaxParserUtil.peekNextStartElement(xmlEventReader);
            if (subEvent == null)
                break;
            String elementName = StaxParserUtil.getStartElementName(subEvent);

            if (TOKEN_PROVIDER_ELEMENT.equalsIgnoreCase(elementName)) {
                subEvent = StaxParserUtil.getNextStartElement(xmlEventReader);
                StaxParserUtil.validate(subEvent, TOKEN_PROVIDER_ELEMENT);
                TokenProviderType tokenProvider = new TokenProviderType();

                // parse the provider attributes (provider class, token type, token element, token namespace).
                QName attributeQName = new QName("", PROVIDER_CLASS_ATTRIB);
                Attribute attribute = subEvent.getAttributeByName(attributeQName);
                if (attribute != null)
                    tokenProvider.setProviderClass(StaxParserUtil.getAttributeValue(attribute));
                attributeQName = new QName("", TOKEN_TYPE_ATTRIB);
                attribute = subEvent.getAttributeByName(attributeQName);
                if (attribute != null)
                    tokenProvider.setTokenType(StaxParserUtil.getAttributeValue(attribute));
                attributeQName = new QName("", TOKEN_ELEMENT_ATTRIB);
                attribute = subEvent.getAttributeByName(attributeQName);
                if (attribute != null)
                    tokenProvider.setTokenElement(StaxParserUtil.getAttributeValue(attribute));
                attributeQName = new QName("", TOKEN_ELEMENT_NS_ATTRIB);
                attribute = subEvent.getAttributeByName(attributeQName);
                if (attribute != null)
                    tokenProvider.setTokenElementNS(StaxParserUtil.getAttributeValue(attribute));

                // parse the provider properties.
                while (xmlEventReader.hasNext()) {
                    xmlEvent = StaxParserUtil.peek(xmlEventReader);
                    if (xmlEvent == null)
                        break;
                    if (xmlEvent instanceof EndElement) {
                        EndElement endElement = (EndElement) StaxParserUtil.getNextEvent(xmlEventReader);
                        String endElementName = StaxParserUtil.getEndElementName(endElement);
                        if (endElementName.equals(TOKEN_PROVIDER_ELEMENT))
                            break;
                        else
                            throw logger.parserUnknownEndElement(endElementName);
                    }

                    subEvent = StaxParserUtil.peekNextStartElement(xmlEventReader);
                    if (subEvent == null)
                        break;
                    elementName = StaxParserUtil.getStartElementName(subEvent);
                    if (PROPERTY_ELEMENT.equalsIgnoreCase(elementName)) {
                        // parse the property key and value.
                        subEvent = StaxParserUtil.getNextStartElement(xmlEventReader);
                        KeyValueType keyValue = new KeyValueType();
                        // parse the key and value attributes.
                        attributeQName = new QName("", KEY_ATTRIB);
                        attribute = subEvent.getAttributeByName(attributeQName);
                        if (attribute != null)
                            keyValue.setKey(StaxParserUtil.getAttributeValue(attribute));
                        attributeQName = new QName("", VALUE_ATTRIB);
                        attribute = subEvent.getAttributeByName(attributeQName);
                        if (attribute != null)
                            keyValue.setValue(StaxParserUtil.getAttributeValue(attribute));

                        EndElement endElement = StaxParserUtil.getNextEndElement(xmlEventReader);
                        StaxParserUtil.validate(endElement, PROPERTY_ELEMENT);
                        tokenProvider.add(keyValue);
                    } else
                        throw logger.parserUnknownTag(elementName, subEvent.getLocation());
                }
                tokenProviders.add(tokenProvider);
            } else
                throw logger.parserUnknownTag(elementName, subEvent.getLocation());
        }
        return tokenProviders;
    }

    /**
     * <p>
     * Parses the {@code ServiceProviders} section of the STS configuration file.
     * </p>
     *
     * @param xmlEventReader the reader used to parse the XML configuration file.
     * @return a {@code ServiceProvidersType} instance that contains the parsed data.
     * @throws ParsingException if an error occurs while parsing the XML file.
     */
    private ServiceProvidersType parseServiceProviders(XMLEventReader xmlEventReader) throws ParsingException {
        StartElement startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
        StaxParserUtil.validate(startElement, SERVICE_PROVIDERS_ELEMENT);

        ServiceProvidersType serviceProviders = new ServiceProvidersType();

        // parse all token providers one by one.
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = StaxParserUtil.peek(xmlEventReader);
            if (xmlEvent == null)
                break;
            if (xmlEvent instanceof EndElement) {
                EndElement endElement = (EndElement) StaxParserUtil.getNextEvent(xmlEventReader);
                String endElementName = StaxParserUtil.getEndElementName(endElement);
                if (endElementName.equals(SERVICE_PROVIDERS_ELEMENT))
                    break;
                else
                    throw logger.parserUnknownEndElement(endElementName);
            }

            StartElement subEvent = StaxParserUtil.peekNextStartElement(xmlEventReader);
            if (subEvent == null)
                break;
            String elementName = StaxParserUtil.getStartElementName(subEvent);

            if (SERVICE_PROVIDER_ELEMENT.equalsIgnoreCase(elementName)) {
                subEvent = StaxParserUtil.getNextStartElement(xmlEventReader);
                StaxParserUtil.validate(subEvent, SERVICE_PROVIDER_ELEMENT);
                ServiceProviderType serviceProvider = new ServiceProviderType();

                // parse the provider attributes (endpoint, token type and truststore alias).
                QName attributeQName = new QName("", TOKEN_TYPE_ATTRIB);
                Attribute attribute = subEvent.getAttributeByName(attributeQName);
                if (attribute != null)
                    serviceProvider.setTokenType(StaxParserUtil.getAttributeValue(attribute));
                attributeQName = new QName("", ENDPOINT_ATTRIB);
                attribute = subEvent.getAttributeByName(attributeQName);
                if (attribute != null)
                    serviceProvider.setEndpoint(StaxParserUtil.getAttributeValue(attribute));
                attributeQName = new QName("", ENDPOINT_REGEX_ATTRIB);
                attribute = subEvent.getAttributeByName(attributeQName);
                if (attribute != null)
                    serviceProvider.setEndpointRegEx(StaxParserUtil.getAttributeValue(attribute));
                attributeQName = new QName("", TRUSTSTORE_ALIAS_ATTRIB);
                attribute = subEvent.getAttributeByName(attributeQName);
                if (attribute != null)
                    serviceProvider.setTruststoreAlias(StaxParserUtil.getAttributeValue(attribute));

                EndElement endElement = StaxParserUtil.getNextEndElement(xmlEventReader);
                StaxParserUtil.validate(endElement, SERVICE_PROVIDER_ELEMENT);
                serviceProviders.add(serviceProvider);
            } else
                throw logger.parserUnknownTag(elementName, subEvent.getLocation());
        }
        return serviceProviders;
    }
}
