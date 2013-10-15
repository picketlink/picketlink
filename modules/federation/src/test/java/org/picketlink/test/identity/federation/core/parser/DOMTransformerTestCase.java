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
package org.picketlink.test.identity.federation.core.parser;

import org.junit.Test;
import org.picketlink.common.util.DocumentUtil;
import org.picketlink.common.util.StaxParserUtil;
import org.picketlink.common.util.TransformerUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stax.StAXSource;
import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Parse an xml file partially using StAX and then use JAXP Transformer to parse a DOM Element and resume stax
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 22, 2010
 */
public class DOMTransformerTestCase {

    String xml = "<a xmlns=\'urn:a\'><b><c><d>SomeD</d></c></b></a>";

    @Test
    public void testDOMTransformer() throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(xml.getBytes());
        XMLEventReader xmlEventReader = StaxParserUtil.getXMLEventReader(bis);

        StartElement a = StaxParserUtil.getNextStartElement(xmlEventReader);
        StaxParserUtil.validate(a, "a");

        Document resultDocument = DocumentUtil.createDocument();
        DOMResult domResult = new DOMResult(resultDocument);

        // Let us parse <b><c><d> using transformer
        StAXSource source = new StAXSource(xmlEventReader);

        Transformer transformer = TransformerUtil.getStaxSourceToDomResultTransformer();
        transformer.transform(source, domResult);

        Document doc = (Document) domResult.getNode();
        Element elem = doc.getDocumentElement();
        assertEquals("b", elem.getLocalName());

        XMLEvent xmlEvent = xmlEventReader.nextEvent();
        assertTrue(xmlEvent instanceof EndElement);
        StaxParserUtil.validate((EndElement) xmlEvent, "a");
    }
}