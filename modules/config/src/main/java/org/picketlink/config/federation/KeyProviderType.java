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
 *
 * Source of the Signing and Validating Key
 *
 *
 * <p>
 * Java class for KeyProviderType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="KeyProviderType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Auth" type="{urn:picketlink:identity-federation:config:1.0}AuthPropertyType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ValidatingAlias" type="{urn:picketlink:identity-federation:config:1.0}KeyValueType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="SigningAlias" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="ClassName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public class KeyProviderType {

    protected List<AuthPropertyType> auth = new ArrayList<AuthPropertyType>();

    protected List<KeyValueType> validatingAlias = new ArrayList<KeyValueType>();

    protected String signingAlias;

    protected String className;

    public void add(AuthPropertyType kv) {
        this.auth.add(kv);
    }

    public void remove(AuthPropertyType kv) {
        this.auth.remove(kv);
    }

    /**
     * Gets the value of the auth property.
     * <p>
     * Objects of the following type(s) are allowed in the list {@link AuthPropertyType }
     *
     *
     */
    public List<AuthPropertyType> getAuth() {
        return Collections.unmodifiableList(this.auth);
    }

    public void add(KeyValueType kv) {
        this.validatingAlias.add(kv);
    }

    public void remove(KeyValueType kv) {
        this.validatingAlias.remove(kv);
    }

    /**
     * Gets the value of the validatingAlias property.
     * <p>
     * Objects of the following type(s) are allowed in the list {@link KeyValueType }
     *
     *
     */
    public List<KeyValueType> getValidatingAlias() {
        return Collections.unmodifiableList(this.validatingAlias);
    }

    /**
     * Gets the value of the signingAlias property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getSigningAlias() {
        return signingAlias;
    }

    /**
     * Sets the value of the signingAlias property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setSigningAlias(String value) {
        this.signingAlias = value;
    }

    /**
     * Gets the value of the className property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getClassName() {
        return className;
    }

    /**
     * Sets the value of the className property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setClassName(String value) {
        this.className = value;
    }

}
