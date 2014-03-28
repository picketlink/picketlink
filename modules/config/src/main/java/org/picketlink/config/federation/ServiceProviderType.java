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
 *
 * The service provider type contains information about a specific service provider. In particular, it specifies the type of the
 * token that must be issued for the provider and the alias of the provider's PKC in the truststore. This is used by the STS to
 * locate the PKC when encrypting the generated token.
 *
 *
 * <p>
 * Java class for ServiceProviderType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ServiceProviderType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="Endpoint" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="TruststoreAlias" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *        &lt;attribute name="TokenType" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="TruststoreAlias" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public class ServiceProviderType {

    protected String endpoint;

    protected String tokenType;

    protected String truststoreAlias;

    private String endpointRegEx;

    /**
     * Gets the value of the endpoint property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the value of the endpoint property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setEndpoint(String value) {
        this.endpoint = value;
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
     * Gets the value of the truststoreAlias property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getTruststoreAlias() {
        return truststoreAlias;
    }

    /**
     * Sets the value of the truststoreAlias property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setTruststoreAlias(String value) {
        this.truststoreAlias = value;
    }

    public void setEndpointRegEx(String endpointRegEx) {
        this.endpointRegEx = endpointRegEx;
    }

    public String getEndpointRegEx() {
        return endpointRegEx;
    }
}
