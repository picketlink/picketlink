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
package org.picketlink.identity.federation.ws.wss.utility;

import org.picketlink.identity.federation.ws.addressing.AnyAddressingType;

/**
 * This complex type ties together the timestamp related elements into a composite type.
 *
 *
 * <p>
 * Java class for TimestampType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="TimestampType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd}Created"
 * minOccurs="0"/>
 *         &lt;element ref="{http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd}Expires"
 * minOccurs="0"/>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;any/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd}commonAtts"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class TimestampType extends AnyAddressingType {

    protected AttributedDateTime created;

    protected AttributedDateTime expires;

    protected String id;

    /**
     * Gets the value of the created property.
     *
     * @return possible object is {@link AttributedDateTime }
     */
    public AttributedDateTime getCreated() {
        return created;
    }

    /**
     * Sets the value of the created property.
     *
     * @param value allowed object is {@link AttributedDateTime }
     */
    public void setCreated(AttributedDateTime value) {
        this.created = value;
    }

    /**
     * Gets the value of the expires property.
     *
     * @return possible object is {@link AttributedDateTime }
     */
    public AttributedDateTime getExpires() {
        return expires;
    }

    /**
     * Sets the value of the expires property.
     *
     * @param value allowed object is {@link AttributedDateTime }
     */
    public void setExpires(AttributedDateTime value) {
        this.expires = value;
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