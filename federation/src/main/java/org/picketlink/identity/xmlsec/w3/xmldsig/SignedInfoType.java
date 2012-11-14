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
package org.picketlink.identity.xmlsec.w3.xmldsig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Java class for SignedInfoType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="SignedInfoType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}CanonicalizationMethod"/>
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}SignatureMethod"/>
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}Reference" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public class SignedInfoType {
    protected CanonicalizationMethodType canonicalizationMethod;
    protected SignatureMethodType signatureMethod;
    protected List<ReferenceType> reference = new ArrayList<ReferenceType>();
    protected String id;

    /**
     * Gets the value of the canonicalizationMethod property.
     *
     * @return possible object is {@link CanonicalizationMethodType }
     *
     */
    public CanonicalizationMethodType getCanonicalizationMethod() {
        return canonicalizationMethod;
    }

    /**
     * Sets the value of the canonicalizationMethod property.
     *
     * @param value allowed object is {@link CanonicalizationMethodType }
     *
     */
    public void setCanonicalizationMethod(CanonicalizationMethodType value) {
        this.canonicalizationMethod = value;
    }

    /**
     * Gets the value of the signatureMethod property.
     *
     * @return possible object is {@link SignatureMethodType }
     *
     */
    public SignatureMethodType getSignatureMethod() {
        return signatureMethod;
    }

    /**
     * Sets the value of the signatureMethod property.
     *
     * @param value allowed object is {@link SignatureMethodType }
     *
     */
    public void setSignatureMethod(SignatureMethodType value) {
        this.signatureMethod = value;
    }

    public void add(ReferenceType ref) {
        this.reference.add(ref);
    }

    public void remove(ReferenceType ref) {
        this.reference.remove(ref);
    }

    /**
     * Gets the value of the reference property.
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link ReferenceType }
     *
     *
     */
    public List<ReferenceType> getReference() {
        return Collections.unmodifiableList(this.reference);
    }

    /**
     * Gets the value of the id property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setId(String value) {
        this.id = value;
    }

}
