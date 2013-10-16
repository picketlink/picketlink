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

import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.constants.WSTrustConstants;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.common.util.StaxUtil;
import org.picketlink.common.util.StringUtil;
import org.picketlink.identity.federation.ws.wss.secext.AttributedString;
import org.picketlink.identity.federation.ws.wss.secext.KeyIdentifierType;
import org.picketlink.identity.federation.ws.wss.secext.SecurityTokenReferenceType;
import org.picketlink.identity.federation.ws.wss.secext.UsernameTokenType;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.picketlink.identity.federation.core.wsse.WSSecurityConstants.ID;
import static org.picketlink.identity.federation.core.wsse.WSSecurityConstants.USERNAME;
import static org.picketlink.identity.federation.core.wsse.WSSecurityConstants.USERNAME_TOKEN;
import static org.picketlink.identity.federation.core.wsse.WSSecurityConstants.WSSE_NS;
import static org.picketlink.identity.federation.core.wsse.WSSecurityConstants.WSSE_PREFIX;
import static org.picketlink.identity.federation.core.wsse.WSSecurityConstants.WSU_NS;
import static org.picketlink.identity.federation.core.wsse.WSSecurityConstants.WSU_PREFIX;

/**
 * Write WS-Security Elements
 *
 * @author Anil.Saldhana@redhat.com
 * @since Nov 8, 2010
 */
public class WSSecurityWriter {

    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    private final XMLStreamWriter writer;

    public WSSecurityWriter(XMLStreamWriter writer) {
        this.writer = writer;
    }

    public void write(UsernameTokenType usernameToken) throws ProcessingException {
        StaxUtil.writeStartElement(writer, WSSE_PREFIX, USERNAME_TOKEN, WSSE_NS);
        StaxUtil.writeNameSpace(writer, WSSE_PREFIX, WSSE_NS);

        String id = usernameToken.getId();
        if (StringUtil.isNullOrEmpty(id))
            throw logger.nullValueError("Id on the UsernameToken");

        QName wsuIDQName = new QName(WSU_NS, ID, WSU_PREFIX);
        StaxUtil.writeNameSpace(writer, WSU_PREFIX, WSU_NS);
        StaxUtil.writeAttribute(writer, wsuIDQName, id);

        AttributedString userNameAttr = usernameToken.getUsername();
        if (userNameAttr == null)
            throw logger.nullValueError("User Name is null on the UsernameToken");

        StaxUtil.writeStartElement(writer, WSSE_PREFIX, USERNAME, WSSE_NS);
        StaxUtil.writeCharacters(writer, userNameAttr.getValue());
        StaxUtil.writeEndElement(writer);

        StaxUtil.writeEndElement(writer);
        StaxUtil.flush(writer);
    }

    public void writeLifetime(XMLGregorianCalendar created, XMLGregorianCalendar expires) throws ProcessingException {
        // write the created element.
        StaxUtil.writeStartElement(this.writer, WSU_PREFIX, WSTrustConstants.CREATED, WSU_NS);
        StaxUtil.writeNameSpace(this.writer, WSU_PREFIX, WSU_NS);
        StaxUtil.writeCharacters(this.writer, created.toXMLFormat());
        StaxUtil.writeEndElement(this.writer);

        // write the expires element.
        StaxUtil.writeStartElement(this.writer, WSU_PREFIX, WSTrustConstants.EXPIRES, WSU_NS);
        StaxUtil.writeNameSpace(this.writer, WSU_PREFIX, WSU_NS);
        StaxUtil.writeCharacters(this.writer, expires.toXMLFormat());
        StaxUtil.writeEndElement(this.writer);

        StaxUtil.flush(this.writer);
    }

    public void writeSecurityTokenReference(SecurityTokenReferenceType secRef) throws ProcessingException {
        Set<String> usedNamespaces = new HashSet<String>();
        usedNamespaces.add(WSSE_NS);

        StaxUtil.writeStartElement(writer, WSSE_PREFIX, WSTrustConstants.WSSE.SECURITY_TOKEN_REFERENCE, WSSE_NS);
        StaxUtil.writeNameSpace(writer, WSSE_PREFIX, WSSE_NS);

        // write the id attribute, if available.
        if (secRef.getId() != null && secRef.getId() != "") {
            QName wsuIDQName = new QName(WSU_NS, ID, WSU_PREFIX);
            StaxUtil.writeNameSpace(writer, WSU_PREFIX, WSU_NS);
            StaxUtil.writeAttribute(writer, wsuIDQName, secRef.getId());
            usedNamespaces.add(WSU_NS);
        }

        // write all other attributes.
        for (Map.Entry<QName, String> entry : secRef.getOtherAttributes().entrySet()) {
            QName key = entry.getKey();
            // check if the namespace needs to be written.
            if (!usedNamespaces.contains(key.getNamespaceURI())) {
                StaxUtil.writeNameSpace(this.writer, key.getPrefix(), key.getNamespaceURI());
                usedNamespaces.add(key.getNamespaceURI());
            }
            StaxUtil.writeAttribute(this.writer, key, entry.getValue());
        }

        // write the key identifier, if available.
        for (Object obj : secRef.getAny()) {
            if (obj instanceof KeyIdentifierType) {
                KeyIdentifierType keyId = (KeyIdentifierType) obj;
                StaxUtil.writeStartElement(this.writer, WSSE_PREFIX, WSTrustConstants.WSSE.KEY_IDENTIFIER, WSSE_NS);
                StaxUtil.writeAttribute(this.writer, WSTrustConstants.WSSE.VALUE_TYPE, keyId.getValueType());
                StaxUtil.writeCharacters(this.writer, keyId.getValue());
                StaxUtil.writeEndElement(this.writer);
            }
        }

        StaxUtil.writeEndElement(this.writer);
        StaxUtil.flush(this.writer);
    }
}