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
 * Actual content model is non-deterministic, hence wildcard. The following shows intended content model:
 *
 * <xs:element ref='wst:TokenType' minOccurs='0' /> <xs:element ref='wst:RequestType' /> <xs:element
 * ref='wst:RequestedSecurityToken' minOccurs='0' /> <xs:element ref='wsp:AppliesTo' minOccurs='0' /> <xs:element
 * ref='wst:RequestedAttachedReference' minOccurs='0' /> <xs:element ref='wst:RequestedUnattachedReference'
 * minOccurs='0' />
 * <xs:element ref='wst:RequestedProofToken' minOccurs='0' /> <xs:element ref='wst:Entropy' minOccurs='0' />
 * <xs:element
 * ref='wst:Lifetime' minOccurs='0' /> <xs:element ref='wst:Status' minOccurs='0' /> <xs:element
 * ref='wst:AllowPostdating'
 * minOccurs='0' /> <xs:element ref='wst:Renewing' minOccurs='0' /> <xs:element ref='wst:OnBehalfOf' minOccurs='0' />
 * <xs:element ref='wst:Issuer' minOccurs='0' /> <xs:element ref='wst:AuthenticationType' minOccurs='0' /> <xs:element
 * ref='wst:Authenticator' minOccurs='0' /> <xs:element ref='wst:KeyType' minOccurs='0' /> <xs:element
 * ref='wst:KeySize'
 * minOccurs='0' /> <xs:element ref='wst:SignatureAlgorithm' minOccurs='0' /> <xs:element ref='wst:Encryption'
 * minOccurs='0' />
 * <xs:element ref='wst:EncryptionAlgorithm' minOccurs='0' /> <xs:element ref='wst:CanonicalizationAlgorithm'
 * minOccurs='0' />
 * <xs:element ref='wst:ProofEncryption' minOccurs='0' /> <xs:element ref='wst:UseKey' minOccurs='0' /> <xs:element
 * ref='wst:SignWith' minOccurs='0' /> <xs:element ref='wst:EncryptWith' minOccurs='0' /> <xs:element
 * ref='wst:DelegateTo'
 * minOccurs='0' /> <xs:element ref='wst:Forwardable' minOccurs='0' /> <xs:element ref='wst:Delegatable' minOccurs='0'
 * />
 * <xs:element ref='wsp:Policy' minOccurs='0' /> <xs:element ref='wsp:PolicyReference' minOccurs='0' /> <xs:any
 * namespace='##other' processContents='lax' minOccurs='0' maxOccurs='unbounded' />
 *
 *
 *
 * <p>
 * Java class for RequestSecurityTokenResponseType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="RequestSecurityTokenResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;any/>
 *       &lt;/sequence>
 *       &lt;attribute name="Context" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class RequestSecurityTokenResponseType extends AnyAddressingType {

    protected String context;

    /**
     * Gets the value of the context property.
     *
     * @return possible object is {@link String }
     */
    public String getContext() {
        return context;
    }

    /**
     * Sets the value of the context property.
     *
     * @param value allowed object is {@link String }
     */
    public void setContext(String value) {
        this.context = value;
    }
}