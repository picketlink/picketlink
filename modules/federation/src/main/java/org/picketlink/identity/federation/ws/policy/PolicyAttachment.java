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

import org.picketlink.identity.federation.ws.addressing.AnyAddressingType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
 *       &lt;sequence>
 *         &lt;element ref="{http://schemas.xmlsoap.org/ws/2004/09/policy}AppliesTo"/>
 *         &lt;choice maxOccurs="unbounded">
 *           &lt;element ref="{http://schemas.xmlsoap.org/ws/2004/09/policy}Policy"/>
 *           &lt;element ref="{http://schemas.xmlsoap.org/ws/2004/09/policy}PolicyReference"/>
 *         &lt;/choice>
 *         &lt;any/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"appliesTo", "policyOrPolicyReference", "any"})
@XmlRootElement(name = "PolicyAttachment")
public class PolicyAttachment extends AnyAddressingType {

    protected AppliesTo appliesTo;

    protected List<PolicyChoice> theChoices = new ArrayList<PolicyChoice>();

    public static class PolicyChoice {

        private Policy thePolicy;

        private PolicyReference thePolicyRef;

        public PolicyChoice(Policy p) {
            thePolicy = p;
        }

        public PolicyChoice(PolicyReference pr) {
            thePolicyRef = pr;
        }

        public Policy getPolicy() {
            return thePolicy;
        }

        public PolicyReference getPolicyReference() {
            return thePolicyRef;
        }
    }

    /**
     * Gets the value of the appliesTo property.
     *
     * @return possible object is {@link AppliesTo }
     */
    public AppliesTo getAppliesTo() {
        return appliesTo;
    }

    /**
     * Sets the value of the appliesTo property.
     *
     * @param value allowed object is {@link AppliesTo }
     */
    public void setAppliesTo(AppliesTo value) {
        this.appliesTo = value;
    }

    /**
     * Add a {@link PolicyChoice}
     *
     * @param pc
     */
    public void addChoice(PolicyChoice pc) {
        this.theChoices.add(pc);
    }

    /**
     * Gets the value of the policyOrPolicyReference property.
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link PolicyReference } {@link Policy }
     */
    public List<PolicyChoice> getPolicyOrPolicyReference() {
        return Collections.unmodifiableList(this.theChoices);
    }
}