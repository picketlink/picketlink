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

/**
 * Aspects involved in trust decisions such as the domains that the IDP or the Service Provider trusts.
 *
 * <p>
 * Java class for TrustType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="TrustType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Domains" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class TrustType {

    protected String domains;

    /**
     * Gets the value of the domains property.
     *
     * @return possible object is {@link String }
     */
    public String getDomains() {
        return domains;
    }

    /**
     * Sets the value of the domains property.
     *
     * @param value allowed object is {@link String }
     */
    public void setDomains(String value) {
        this.domains = value;
    }

    /**
     * <p>Adds a new domain to the list of trusted domains.</p>
     *
     * @param domain
     */
    public void addDomain(String domain) {
        if (this.domains == null) {
            this.domains = "";
        }

        if (!this.domains.isEmpty()) {
            domain = "," + domain;
        }

        this.domains = this.domains + domain;
    }

}
