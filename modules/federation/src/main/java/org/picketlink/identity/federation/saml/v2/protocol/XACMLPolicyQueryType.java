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
package org.picketlink.identity.federation.saml.v2.protocol;

import org.jboss.security.xacml.core.model.context.RequestType;
import org.jboss.security.xacml.core.model.policy.IdReferenceType;
import org.jboss.security.xacml.core.model.policy.TargetType;

import javax.xml.datatype.XMLGregorianCalendar;

/**
 * <p>
 * Java class for XACMLPolicyQueryType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="XACMLPolicyQueryType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:SAML:2.0:protocol}RequestAbstractType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element ref="{urn:oasis:names:tc:xacml:2.0:context:schema:os}Request"/>
 *         &lt;element ref="{urn:oasis:names:tc:xacml:2.0:policy:schema:os}Target"/>
 *         &lt;element ref="{urn:oasis:names:tc:xacml:2.0:policy:schema:os}PolicySetIdReference"/>
 *         &lt;element ref="{urn:oasis:names:tc:xacml:2.0:policy:schema:os}PolicyIdReference"/>
 *       &lt;/choice>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class XACMLPolicyQueryType extends RequestAbstractType {

    private static final long serialVersionUID = 1L;

    public static class ChoiceType {

        private RequestType request;

        private TargetType target;

        private IdReferenceType policySetIDReference;

        private IdReferenceType policyIdReference;

        public RequestType getRequest() {
            return request;
        }

        public void setRequest(RequestType request) {
            this.request = request;
        }

        public TargetType getTarget() {
            return target;
        }

        public void setTarget(TargetType target) {
            this.target = target;
        }

        public IdReferenceType getPolicySetIDReference() {
            return policySetIDReference;
        }

        public void setPolicySetIDReference(IdReferenceType policySetIDReference) {
            this.policySetIDReference = policySetIDReference;
        }

        public IdReferenceType getPolicyIdReference() {
            return policyIdReference;
        }

        public void setPolicyIdReference(IdReferenceType policyIdReference) {
            this.policyIdReference = policyIdReference;
        }
    }

    protected ChoiceType choiceType;

    public XACMLPolicyQueryType(String id, XMLGregorianCalendar instant) {
        super(id, instant);
    }

    public ChoiceType getChoiceType() {
        return choiceType;
    }

    public void setChoiceType(ChoiceType choiceType) {
        this.choiceType = choiceType;
    }
}