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
     */
    public CanonicalizationMethodType getCanonicalizationMethod() {
        return canonicalizationMethod;
    }

    /**
     * Sets the value of the canonicalizationMethod property.
     *
     * @param value allowed object is {@link CanonicalizationMethodType }
     */
    public void setCanonicalizationMethod(CanonicalizationMethodType value) {
        this.canonicalizationMethod = value;
    }

    /**
     * Gets the value of the signatureMethod property.
     *
     * @return possible object is {@link SignatureMethodType }
     */
    public SignatureMethodType getSignatureMethod() {
        return signatureMethod;
    }

    /**
     * Sets the value of the signatureMethod property.
     *
     * @param value allowed object is {@link SignatureMethodType }
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
     */
    public List<ReferenceType> getReference() {
        return Collections.unmodifiableList(this.reference);
    }

    /**
     * Gets the value of the id property.
     *
     * @return possible object is {@link String }
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value allowed object is {@link String }
     */
    public void setId(String value) {
        this.id = value;
    }

}
