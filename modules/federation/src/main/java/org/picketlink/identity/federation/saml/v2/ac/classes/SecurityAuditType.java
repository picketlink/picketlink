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

package org.picketlink.identity.federation.saml.v2.ac.classes;

/**
 * <p>
 * Java class for SecurityAuditType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="SecurityAuditType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}SwitchAudit" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}Extension"
 * maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class SecurityAuditType extends ExtensionListType {

    protected ExtensionOnlyType switchAudit;

    /**
     * Gets the value of the switchAudit property.
     *
     * @return possible object is {@link ExtensionOnlyType }
     */
    public ExtensionOnlyType getSwitchAudit() {
        return switchAudit;
    }

    /**
     * Sets the value of the switchAudit property.
     *
     * @param value allowed object is {@link ExtensionOnlyType }
     */
    public void setSwitchAudit(ExtensionOnlyType value) {
        this.switchAudit = value;
    }
}