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
package org.picketlink.config.federation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Java class for TokenProviderType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="TokenProviderType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Property" type="{urn:picketlink:identity-federation:config:1.0}KeyValueType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="ProviderClass" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="TokenType" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="TokenElement" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="TokenElementNS" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public class TokenProviderType {

    protected List<KeyValueType> property = new ArrayList<KeyValueType>();

    protected String providerClass;

    protected String tokenType;

    protected String tokenElement;

    protected String tokenElementNS;

    public void add(KeyValueType kv) {
        property.add(kv);
    }

    public void remove(KeyValueType kv) {
        this.remove(kv);
    }

    /**
     * Gets the value of the property property.
     * <p>
     * Objects of the following type(s) are allowed in the list {@link KeyValueType }
     *
     *
     */
    public List<KeyValueType> getProperty() {
        return Collections.unmodifiableList(this.property);
    }

    /**
     * Gets the value of the providerClass property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getProviderClass() {
        return providerClass;
    }

    /**
     * Sets the value of the providerClass property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setProviderClass(String value) {
        this.providerClass = value;
    }

    /**
     * Gets the value of the tokenType property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getTokenType() {
        return tokenType;
    }

    /**
     * Sets the value of the tokenType property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setTokenType(String value) {
        this.tokenType = value;
    }

    /**
     * Gets the value of the tokenElement property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getTokenElement() {
        return tokenElement;
    }

    /**
     * Sets the value of the tokenElement property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setTokenElement(String value) {
        this.tokenElement = value;
    }

    /**
     * Gets the value of the tokenElementNS property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getTokenElementNS() {
        return tokenElementNS;
    }

    /**
     * Sets the value of the tokenElementNS property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setTokenElementNS(String value) {
        this.tokenElementNS = value;
    }

}
