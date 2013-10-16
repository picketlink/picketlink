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
package org.picketlink.test.identity.federation.core.stax;

import org.junit.Test;
import org.picketlink.common.util.DocumentUtil;
import org.picketlink.common.util.StaxUtil;
import org.w3c.dom.Document;

import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;

/**
 * Test how we write a DOM Element to Stax writer
 *
 * @author Anil.Saldhana@redhat.com
 * @since Nov 8, 2010
 */
public class DomElementToStaxWritingTestCase {

    @Test
    public void testDOM2Stax() throws Exception {
        String xml = "<a xmlns=\'urn:hello\' >  <b> <c/> <d xmlns=\'urn:t\' test=\'tt\'/> </b></a>";

        Document doc = DocumentUtil.getDocument(xml);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        XMLStreamWriter writer = StaxUtil.getXMLStreamWriter(baos);
        StaxUtil.writeDOMElement(writer, doc.getDocumentElement());

        String writtenDoc = new String(baos.toByteArray());
        doc = DocumentUtil.getDocument(writtenDoc);
    }
}