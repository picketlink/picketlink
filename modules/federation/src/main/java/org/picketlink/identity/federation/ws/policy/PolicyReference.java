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

package org.picketlink.identity.federation.ws.policy;

import org.picketlink.identity.federation.ws.addressing.BaseAddressingType;

/**
 * <p>
 * Java class for anonymous complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="URI" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       &lt;attribute name="Digest" type="{http://www.w3.org/2001/XMLSchema}base64Binary" />
 *       &lt;attribute name="DigestAlgorithm" type="{http://www.w3.org/2001/XMLSchema}anyURI"
 * default="http://schemas.xmlsoap.org/ws/2004/09/policy/Sha1Exc" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class PolicyReference extends BaseAddressingType {

    protected String uri;

    protected byte[] digest;

    protected String digestAlgorithm;

    /**
     * Gets the value of the uri property.
     *
     * @return possible object is {@link String }
     */
    public String getURI() {
        return uri;
    }

    /**
     * Sets the value of the uri property.
     *
     * @param value allowed object is {@link String }
     */
    public void setURI(String value) {
        this.uri = value;
    }

    /**
     * Gets the value of the digest property.
     *
     * @return possible object is byte[]
     */
    public byte[] getDigest() {
        return digest;
    }

    /**
     * Sets the value of the digest property.
     *
     * @param value allowed object is byte[]
     */
    public void setDigest(byte[] value) {
        this.digest = (value);
    }

    /**
     * Gets the value of the digestAlgorithm property.
     *
     * @return possible object is {@link String }
     */
    public String getDigestAlgorithm() {
        if (digestAlgorithm == null) {
            return "http://schemas.xmlsoap.org/ws/2004/09/policy/Sha1Exc";
        } else {
            return digestAlgorithm;
        }
    }

    /**
     * Sets the value of the digestAlgorithm property.
     *
     * @param value allowed object is {@link String }
     */
    public void setDigestAlgorithm(String value) {
        this.digestAlgorithm = value;
    }
}