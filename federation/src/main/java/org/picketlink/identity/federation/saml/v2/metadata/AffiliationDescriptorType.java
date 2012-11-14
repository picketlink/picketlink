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
package org.picketlink.identity.federation.saml.v2.metadata;

import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.picketlink.identity.xmlsec.w3.xmldsig.SignatureType;

/**
 * <p>
 * Java class for AffiliationDescriptorType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="AffiliationDescriptorType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}Signature" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:metadata}Extensions" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:metadata}AffiliateMember" maxOccurs="unbounded"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:metadata}KeyDescriptor" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="affiliationOwnerID" use="required" type="{urn:oasis:names:tc:SAML:2.0:metadata}entityIDType" />
 *       &lt;attribute name="validUntil" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="cacheDuration" type="{http://www.w3.org/2001/XMLSchema}duration" />
 *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public class AffiliationDescriptorType extends TypeWithOtherAttributes {
    protected SignatureType signature;

    protected ExtensionsType extensions;

    protected List<String> affiliateMember;

    protected List<KeyDescriptorType> keyDescriptor;

    protected String affiliationOwnerID;

    protected XMLGregorianCalendar validUntil;

    protected Duration cacheDuration;

    protected String id;

    /**
     * Gets the value of the signature property.
     *
     * @return possible object is {@link SignatureType }
     *
     */
    public SignatureType getSignature() {
        return signature;
    }

    /**
     * Sets the value of the signature property.
     *
     * @param value allowed object is {@link SignatureType }
     *
     */
    public void setSignature(SignatureType value) {
        this.signature = value;
    }

    /**
     * Gets the value of the extensions property.
     *
     * @return possible object is {@link ExtensionsType }
     *
     */
    public ExtensionsType getExtensions() {
        return extensions;
    }

    /**
     * Sets the value of the extensions property.
     *
     * @param value allowed object is {@link ExtensionsType }
     *
     */
    public void setExtensions(ExtensionsType value) {
        this.extensions = value;
    }

    /**
     * Gets the value of the affiliateMember property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the
     * affiliateMember property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getAffiliateMember().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link String }
     *
     *
     */
    public List<String> getAffiliateMember() {
        if (affiliateMember == null) {
            affiliateMember = new ArrayList<String>();
        }
        return this.affiliateMember;
    }

    /**
     * Gets the value of the keyDescriptor property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the
     * keyDescriptor property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getKeyDescriptor().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link KeyDescriptorType }
     *
     *
     */
    public List<KeyDescriptorType> getKeyDescriptor() {
        if (keyDescriptor == null) {
            keyDescriptor = new ArrayList<KeyDescriptorType>();
        }
        return this.keyDescriptor;
    }

    /**
     * Gets the value of the affiliationOwnerID property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getAffiliationOwnerID() {
        return affiliationOwnerID;
    }

    /**
     * Sets the value of the affiliationOwnerID property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setAffiliationOwnerID(String value) {
        this.affiliationOwnerID = value;
    }

    /**
     * Gets the value of the validUntil property.
     *
     * @return possible object is {@link XMLGregorianCalendar }
     *
     */
    public XMLGregorianCalendar getValidUntil() {
        return validUntil;
    }

    /**
     * Sets the value of the validUntil property.
     *
     * @param value allowed object is {@link XMLGregorianCalendar }
     *
     */
    public void setValidUntil(XMLGregorianCalendar value) {
        this.validUntil = value;
    }

    /**
     * Gets the value of the cacheDuration property.
     *
     * @return possible object is {@link Duration }
     *
     */
    public Duration getCacheDuration() {
        return cacheDuration;
    }

    /**
     * Sets the value of the cacheDuration property.
     *
     * @param value allowed object is {@link Duration }
     *
     */
    public void setCacheDuration(Duration value) {
        this.cacheDuration = value;
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
     * Sets the value of the id property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setID(String value) {
        this.id = value;
    }
}