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
package org.picketlink.identity.federation.ws.trust;

import org.picketlink.identity.federation.ws.addressing.AnyAddressingType;

/**
 * <p>
 * Java class for SignChallengeType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="SignChallengeType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://docs.oasis-open.org/ws-sx/ws-trust/200512/}Challenge"/>
 *         &lt;any/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class SignChallengeType extends AnyAddressingType {

    protected String challenge;

    /**
     * Gets the value of the challenge property.
     *
     * @return possible object is {@link String }
     */
    public String getChallenge() {
        return challenge;
    }

    /**
     * Sets the value of the challenge property.
     *
     * @param value allowed object is {@link String }
     */
    public void setChallenge(String value) {
        this.challenge = value;
    }
}