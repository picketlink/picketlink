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

package org.picketlink.config;

import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.common.parsers.AbstractParser;
import org.picketlink.common.util.StaxParserUtil;
import org.picketlink.config.federation.PicketLinkType;
import org.picketlink.config.federation.ProviderType;
import org.picketlink.config.federation.STSType;
import org.picketlink.config.federation.handler.Handlers;
import org.picketlink.config.federation.parsers.SAMLConfigParser;
import org.picketlink.config.federation.parsers.STSConfigParser;
import org.picketlink.config.idm.IDMType;
import org.picketlink.config.idm.parsers.IDMConfigParser;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

/**
 * Parser to parse the consolidated picketlink.xml
 *
 * @author anil saldhana
 */
public class PicketLinkConfigParser extends AbstractParser {

    public static final String PICKETLINK = "PicketLink";

    public static final String ENABLE_AUDIT = "EnableAudit";

    @Override
    public Object parse(XMLEventReader xmlEventReader) throws ParsingException {
        PicketLinkType picketLinkType = new PicketLinkType();
        StartElement startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
        StaxParserUtil.validate(startElement, PICKETLINK);

        // parse and set the root element attributes.
        QName attributeQName = new QName("", ENABLE_AUDIT);
        Attribute attribute = startElement.getAttributeByName(attributeQName);
        if (attribute != null) {
            picketLinkType.setEnableAudit(Boolean.parseBoolean(StaxParserUtil.getAttributeValue(attribute)));
        }

        startElement = StaxParserUtil.peekNextStartElement(xmlEventReader);
        String tag = StaxParserUtil.getStartElementName(startElement);
        while (xmlEventReader.hasNext()) {
            if (SAMLConfigParser.IDP.equals(tag)) {
                SAMLConfigParser samlConfigParser = new SAMLConfigParser();
                ProviderType idp = (ProviderType) samlConfigParser.parse(xmlEventReader);
                picketLinkType.setIdpOrSP(idp);
            } else if (SAMLConfigParser.SP.equals(tag)) {
                SAMLConfigParser samlConfigParser = new SAMLConfigParser();
                ProviderType sp = (ProviderType) samlConfigParser.parse(xmlEventReader);
                picketLinkType.setIdpOrSP(sp);
            } else if (SAMLConfigParser.HANDLERS.equals(tag)) {
                SAMLConfigParser samlConfigParser = new SAMLConfigParser();
                Handlers handlers = (Handlers) samlConfigParser.parse(xmlEventReader);
                picketLinkType.setHandlers(handlers);
            } else if (STSConfigParser.ROOT_ELEMENT.equals(tag)) {
                STSConfigParser samlConfigParser = new STSConfigParser();
                STSType sts = (STSType) samlConfigParser.parse(xmlEventReader);
                picketLinkType.setStsType(sts);
            } else if (IDMConfigParser.ROOT_ELEMENT.equals(tag)) {
                IDMConfigParser parser = new IDMConfigParser();
                IDMType idmType = (IDMType) parser.parse(xmlEventReader);
                picketLinkType.setIdmType(idmType);
            }
            // avoid infinite loop if unknown element is found
            else {
                throw logger.parserUnknownStartElement(tag, startElement.getLocation());
            }
            startElement = StaxParserUtil.peekNextStartElement(xmlEventReader);
            if (startElement == null)
                break;
            tag = StaxParserUtil.getStartElementName(startElement);
        }
        return picketLinkType;
    }

    @Override
    public boolean supports(QName qname) {
        return false;
    }
}