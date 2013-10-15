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
package org.picketlink.identity.federation.ws.wss.secext;

import org.picketlink.identity.federation.ws.addressing.AnyAddressingType;
import org.picketlink.identity.federation.ws.trust.SimpleCollectionUsage;

import java.util.Collections;
import java.util.List;

/**
 * This type is used reference a security token.
 *
 * <p>
 * Java class for SecurityTokenReferenceType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="SecurityTokenReferenceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;any/>
 *       &lt;/choice>
 *       &lt;attribute ref="{http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd}Id"/>
 *       &lt;attribute ref="{http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd}Usage"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class SecurityTokenReferenceType extends AnyAddressingType implements SimpleCollectionUsage<String> {

    protected String id;

    protected List<String> usage;

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

    /**
     * Gets the value of the usage property.
     * <p>
     * Objects of the following type(s) are allowed in the list {@link String }
     */
    public List<String> getUsage() {
        return Collections.unmodifiableList(this.usage);
    }

    public void add(String t) {
        this.usage.add(t);
    }

    public boolean remove(String t) {
        return this.usage.remove(t);
    }
}