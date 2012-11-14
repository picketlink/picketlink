/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.picketlink.identity.federation.saml.common;

import java.io.Serializable;

import javax.xml.datatype.XMLGregorianCalendar;

import org.picketlink.identity.xmlsec.w3.xmldsig.SignatureType;
import org.w3c.dom.Element;

/**
 * SAML Request Abstract Type
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public abstract class CommonRequestAbstractType implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String id;

    protected XMLGregorianCalendar issueInstant;

    protected Element signature;

    public CommonRequestAbstractType(String id, XMLGregorianCalendar issueInstant) {
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