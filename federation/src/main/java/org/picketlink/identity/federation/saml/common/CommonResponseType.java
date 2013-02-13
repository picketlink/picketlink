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
package org.picketlink.identity.federation.saml.common;

import java.io.Serializable;

import javax.xml.datatype.XMLGregorianCalendar;

import org.picketlink.identity.xmlsec.w3.xmldsig.SignatureType;
import org.w3c.dom.Element;

/**
 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public class CommonResponseType implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String id;

    protected XMLGregorianCalendar issueInstant;

    protected String inResponseTo;

    protected Element signature;

    public CommonResponseType(String id, XMLGregorianCalendar issueInstant) {
        this.id = id;
        this.issueInstant = issueInstant;
    }

    /**
     * Gets the value of the id property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getID() {
        return id;
    }

    /**
     * Gets the value of the issueInstant property.
     *
     * @return possible object is {@link XMLGregorianCalendar }
     *
     */
    public XMLGregorianCalendar getIssueInstant() {
        return issueInstant;
    }

    /**
     * Gets the value of the inResponseTo property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getInResponseTo() {
        return inResponseTo;
    }

    /**
     * Sets the value of the inResponseTo property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setInResponseTo(String value) {
        this.inResponseTo = value;
    }

    /**
     * Gets the value of the signature property.
     *
     * @return possible object is {@link SignatureType }
     *
     */
    public Element getSignature() {
        return signature;
    }

    /**
     * Sets the value of the signature property.
     *
     * @param value allowed object is {@link SignatureType }
     *
     */
    public void setSignature(Element value) {
        this.signature = value;
    }
}