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
import org.picketlink.identity.federation.ws.addressing.EndpointReferenceType;
import org.picketlink.identity.federation.ws.policy.AppliesTo;

import javax.xml.stream.XMLStreamWriter;
import java.util.List;

import static org.picketlink.common.constants.WSTrustConstants.WSP_NS;
import static org.picketlink.identity.federation.core.wspolicy.WSPolicyConstants.APPLIES_TO;
import static org.picketlink.identity.federation.core.wspolicy.WSPolicyConstants.WSP_PREFIX;

/**
 * Write the WS-Policy Elements
 *
 * @author Anil.Saldhana@redhat.com
 * @since Nov 5, 2010
 */
public class WSPolicyWriter {

    private XMLStreamWriter writer;

    public WSPolicyWriter(XMLStreamWriter writer) {
        this.writer = writer;
    }

    /**
     * Write an {@code AppliesTo} to the stream
     *
     * @param appliesTo
     * @param out
     *
     * @throws ProcessingException
     */
    public void write(AppliesTo appliesTo) throws ProcessingException {
        StaxUtil.writeStartElement(writer, WSP_PREFIX, APPLIES_TO, WSP_NS);
        StaxUtil.writeNameSpace(writer, WSP_PREFIX, WSP_NS);
        StaxUtil.writeCharacters(writer, ""); // Seems like JDK bug - not writing end character

        List<Object> contentList = appliesTo.getAny();
        if (contentList != null) {
            for (Object content : contentList) {
                if (content instanceof EndpointReferenceType) {
                    EndpointReferenceType endpointReference = (EndpointReferenceType) content;
                    WSAddressingWriter wsAddressingWriter = new WSAddressingWriter(this.writer);
                    wsAddressingWriter.write(endpointReference);
                }
            }
        }

        StaxUtil.writeEndElement(writer);
        StaxUtil.flush(writer);
    }
}