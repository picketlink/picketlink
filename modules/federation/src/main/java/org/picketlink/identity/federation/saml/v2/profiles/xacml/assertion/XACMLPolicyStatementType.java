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
package org.picketlink.identity.federation.saml.v2.profiles.xacml.assertion;

import org.jboss.security.xacml.core.model.policy.PolicySetType;
import org.jboss.security.xacml.core.model.policy.PolicyType;
import org.picketlink.identity.federation.saml.v2.assertion.StatementAbstractType;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Java class for XACMLPolicyStatementType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="XACMLPolicyStatementType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:SAML:2.0:assertion}StatementAbstractType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element ref="{urn:oasis:names:tc:xacml:2.0:policy:schema:os}Policy"/>
 *         &lt;element ref="{urn:oasis:names:tc:xacml:2.0:policy:schema:os}PolicySet"/>
 *       &lt;/choice>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class XACMLPolicyStatementType extends StatementAbstractType {

    private static final long serialVersionUID = 1L;

    public static class ChoiceType {

        private PolicyType policy;
        private PolicySetType policySet;

        public PolicyType getPolicy() {
            return policy;
        }

        public void setPolicy(PolicyType policy) {
            this.policy = policy;
        }

        public PolicySetType getPolicySet() {
            return policySet;
        }

        public void setPolicySet(PolicySetType policySet) {
            this.policySet = policySet;
        }
    }

    protected List<ChoiceType> choiceTypeList = new ArrayList<ChoiceType>();

    public void add(ChoiceType choice) {
        choiceTypeList.add(choice);
    }

    /**
     * Gets the value of the choiceTypeList property.
     */
    public List<ChoiceType> getChoiceType() {
        return choiceTypeList;
    }

}