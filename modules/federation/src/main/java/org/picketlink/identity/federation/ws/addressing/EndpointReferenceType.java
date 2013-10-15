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
package org.picketlink.identity.federation.ws.addressing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Java class for EndpointReferenceType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="EndpointReferenceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Address" type="{http://www.w3.org/2005/08/addressing}AttributedURIType"/>
 *         &lt;element ref="{http://www.w3.org/2005/08/addressing}ReferenceParameters" minOccurs="0"/>
 *         &lt;element ref="{http://www.w3.org/2005/08/addressing}Metadata" minOccurs="0"/>
 *         &lt;any/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class EndpointReferenceType extends BaseAddressingType {

    protected AttributedURIType address;

    protected ReferenceParametersType referenceParameters;

    protected MetadataType metadata;

    protected List<Object> any = new ArrayList<Object>();

    /**
     * Gets the value of the address property.
     *
     * @return possible object is {@link AttributedURIType }
     */
    public AttributedURIType getAddress() {
        return address;
    }

    /**
     * Sets the value of the address property.
     *
     * @param value allowed object is {@link AttributedURIType }
     */
    public void setAddress(AttributedURIType value) {
        this.address = value;
    }

    /**
     * Gets the value of the referenceParameters property.
     *
     * @return possible object is {@link ReferenceParametersType }
     */
    public ReferenceParametersType getReferenceParameters() {
        return referenceParameters;
    }

    /**
     * Sets the value of the referenceParameters property.
     *
     * @param value allowed object is {@link ReferenceParametersType }
     */
    public void setReferenceParameters(ReferenceParametersType value) {
        this.referenceParameters = value;
    }

    /**
     * Gets the value of the metadata property.
     *
     * @return possible object is {@link MetadataType }
     */
    public MetadataType getMetadata() {
        return metadata;
    }

    /**
     * Sets the value of the metadata property.
     *
     * @param value allowed object is {@link MetadataType }
     */
    public void setMetadata(MetadataType value) {
        this.metadata = value;
    }

    /**
     * Gets the value of the any property.
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link Object } {@link org.w3c.dom.Element }
     */
    public List<Object> getAny() {
        return Collections.unmodifiableList(this.any);
    }

    /**
     * Add an any
     *
     * @param obj
     */
    public void addAny(Object obj) {
        this.any.add(obj);
    }
}