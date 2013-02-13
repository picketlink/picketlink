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
package org.picketlink.identity.federation.saml.v2.protocol;

import javax.xml.datatype.XMLGregorianCalendar;

import org.picketlink.identity.federation.saml.v2.assertion.EncryptedElementType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;

/**
 * <p>
 * Java class for ManageNameIDRequestType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ManageNameIDRequestType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:SAML:2.0:protocol}RequestAbstractType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}NameID"/>
 *           &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}EncryptedID"/>
 *         &lt;/choice>
 *         &lt;choice>
 *           &lt;element ref="{urn:oasis:names:tc:SAML:2.0:protocol}NewID"/>
 *           &lt;element ref="{urn:oasis:names:tc:SAML:2.0:protocol}NewEncryptedID"/>
 *           &lt;element ref="{urn:oasis:names:tc:SAML:2.0:protocol}Terminate"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public class ManageNameIDRequestType extends RequestAbstractType {
    private static final long serialVersionUID = 1L;

    protected NameIDType nameID;

    protected EncryptedElementType encryptedID;

    protected String newID;

    protected EncryptedElementType newEncryptedID;

    protected TerminateType terminate;

    public ManageNameIDRequestType(String id, XMLGregorianCalendar instant) {
        super(id, instant);
    }

    /**
     * Gets the value of the nameID property.
     *
     * @return possible object is {@link NameIDType }
     *
     */
    public NameIDType getNameID() {
        return nameID;
    }

    /**
     * Sets the value of the nameID property.
     *
     * @param value allowed object is {@link NameIDType }
     *
     */
    public void setNameID(NameIDType value) {
        this.nameID = value;
    }

    /**
     * Gets the value of the encryptedID property.
     *
     * @return possible object is {@link EncryptedElementType }
     *
     */
    public EncryptedElementType getEncryptedID() {
        return encryptedID;
    }

    /**
     * Sets the value of the encryptedID property.
     *
     * @param value allowed object is {@link EncryptedElementType }
     *
     */
    public void setEncryptedID(EncryptedElementType value) {
        this.encryptedID = value;
    }

    /**
     * Gets the value of the newID property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getNewID() {
        return newID;
    }

    /**
     * Sets the value of the newID property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setNewID(String value) {
        this.newID = value;
    }

    /**
     * Gets the value of the newEncryptedID property.
     *
     * @return possible object is {@link EncryptedElementType }
     *
     */
    public EncryptedElementType getNewEncryptedID() {
        return newEncryptedID;
    }

    /**
     * Sets the value of the newEncryptedID property.
     *
     * @param value allowed object is {@link EncryptedElementType }
     *
     */
    public void setNewEncryptedID(EncryptedElementType value) {
        this.newEncryptedID = value;
    }

    /**
     * Gets the value of the terminate property.
     *
     * @return possible object is {@link TerminateType }
     *
     */
    public TerminateType getTerminate() {
        return terminate;
    }

    /**
     * Sets the value of the terminate property.
     *
     * @param value allowed object is {@link TerminateType }
     *
     */
    public void setTerminate(TerminateType value) {
        this.terminate = value;
    }

}