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
package org.picketlink.identity.federation.core.wstrust.writers;

import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.common.util.StaxUtil;
import org.picketlink.identity.federation.ws.addressing.AttributedURIType;
import org.picketlink.identity.federation.ws.addressing.EndpointReferenceType;

import javax.xml.stream.XMLStreamWriter;

import static org.picketlink.identity.federation.core.wsa.WSAddressingConstants.ADDRESS;
import static org.picketlink.identity.federation.core.wsa.WSAddressingConstants.ENDPOINT_REFERENCE;
import static org.picketlink.identity.federation.core.wsa.WSAddressingConstants.WSA_NS;
import static org.picketlink.identity.federation.core.wsa.WSAddressingConstants.WSA_PREFIX;

/**
 * Write WS-Addressing Elements
 *
 * @author Anil.Saldhana@redhat.com
 * @since Nov 5, 2010
 */
public class WSAddressingWriter {

    private XMLStreamWriter writer;

    public WSAddressingWriter(XMLStreamWriter writer) {
        this.writer = writer;
    }

    public void write(EndpointReferenceType endpointReference) throws ProcessingException {
        StaxUtil.writeStartElement(writer, WSA_PREFIX, ENDPOINT_REFERENCE, WSA_NS);
        StaxUtil.writeNameSpace(writer, WSA_PREFIX, WSA_NS);

        AttributedURIType attributedURI = endpointReference.getAddress();
        if (attributedURI != null) {
            String value = attributedURI.getValue();

            StaxUtil.writeStartElement(writer, WSA_PREFIX, ADDRESS, WSA_NS);
            StaxUtil.writeCharacters(writer, value);
            StaxUtil.writeEndElement(writer);
        }

        StaxUtil.writeEndElement(writer);
        StaxUtil.flush(writer);
    }
}