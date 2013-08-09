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

package org.picketlink.config.idm.parsers;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.common.parsers.AbstractParser;
import org.picketlink.common.util.StaxParserUtil;
import org.picketlink.config.idm.ConfigBuilderMethodType;
import org.picketlink.config.idm.IDMType;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * Stax based XML parser for Picketlink IDM configuration
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class IDMConfigParser extends AbstractParser {

    // Root element
    public static final String ROOT_ELEMENT = "PicketLinkIDM";

    public Object parse(XMLEventReader xmlEventReader) throws ParsingException {
        StartElement startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
        StaxParserUtil.validate(startElement, ROOT_ELEMENT);
        IDMType idmType = new IDMType();

        // parse the inner elements
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = StaxParserUtil.peek(xmlEventReader);
            if (xmlEvent == null)
                break;
            else if (xmlEvent instanceof StartElement) {
                startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
                String methodName = StaxParserUtil.getStartElementName(startElement);
                Map<String, String> methodParams = new LinkedHashMap<String, String>();
                Iterator attributes = startElement.getAttributes();
                while (attributes.hasNext()) {
                    Attribute attribute = (Attribute)attributes.next();
                    String attrName = attribute.getName().getLocalPart();
                    String attrValue = StaxParserUtil.getAttributeValue(attribute);
                    methodParams.put(attrName, attrValue);
                }
                ConfigBuilderMethodType configBuilderMethod = new ConfigBuilderMethodType(methodName, methodParams);
                idmType.addConfigBuilderMethod(configBuilderMethod);
            } else if (xmlEvent instanceof EndElement) {
                EndElement endElement = (EndElement) StaxParserUtil.getNextEvent(xmlEventReader);
                String endElementName = StaxParserUtil.getEndElementName(endElement);
                if (endElementName.equals(ROOT_ELEMENT))
                    break;
            } else {
                StaxParserUtil.getNextEvent(xmlEventReader);
            }
        }

        return idmType;
    }

    public boolean supports(QName qname) {
        return false;
    }

}
