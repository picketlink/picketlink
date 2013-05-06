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
 * <p>
 * Java class for EncryptionType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="EncryptionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="EncAlgo" type="{urn:picketlink:identity-federation:config:1.0}EncAlgoType"/>
 *         &lt;element name="KeySize" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public class EncryptionType {

    protected EncAlgoType encAlgo;

    protected int keySize;

    /**
     * Gets the value of the encAlgo property.
     *
     * @return possible object is {@link EncAlgoType }
     *
     */
    public EncAlgoType getEncAlgo() {
        return encAlgo;
    }

    /**
     * Sets the value of the encAlgo property.
     *
     * @param value allowed object is {@link EncAlgoType }
     *
     */
    public void setEncAlgo(EncAlgoType value) {
        this.encAlgo = value;
    }

    /**
     * Gets the value of the keySize property.
     *
     */
    public int getKeySize() {
        return keySize;
    }

    /**
     * Sets the value of the keySize property.
     *
     */
    public void setKeySize(int value) {
        this.keySize = value;
    }

}
