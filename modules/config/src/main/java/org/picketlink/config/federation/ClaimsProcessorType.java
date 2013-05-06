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
 * Java class for ClaimsProcessorType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ClaimsProcessorType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Property" type="{urn:picketlink:identity-federation:config:1.0}KeyValueType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="ProcessorClass" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="Dialect" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public class ClaimsProcessorType {

    protected List<KeyValueType> property = new ArrayList<KeyValueType>();

    protected String processorClass;

    protected String dialect;

    public void add(KeyValueType kv) {
        this.property.add(kv);
    }

    public void remove(KeyValueType kv) {
        this.property.remove(kv);
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
     * Gets the value of the processorClass property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getProcessorClass() {
        return processorClass;
    }

    /**
     * Sets the value of the processorClass property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setProcessorClass(String value) {
        this.processorClass = value;
    }

    /**
     * Gets the value of the dialect property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getDialect() {
        return dialect;
    }

    /**
     * Sets the value of the dialect property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setDialect(String value) {
        this.dialect = value;
    }

}
