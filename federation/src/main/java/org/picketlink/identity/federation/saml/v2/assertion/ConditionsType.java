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
package org.picketlink.identity.federation.saml.v2.assertion;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.picketlink.identity.federation.saml.common.CommonConditionsType;

/**
 * <p>
 * Java class for ConditionsType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ConditionsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}Condition"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}AudienceRestriction"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}OneTimeUse"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}ProxyRestriction"/>
 *       &lt;/choice>
 *       &lt;attribute name="NotBefore" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="NotOnOrAfter" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public class ConditionsType extends CommonConditionsType implements Serializable {
    private static final long serialVersionUID = 1L;

    protected List<ConditionAbstractType> conditions = new ArrayList<ConditionAbstractType>();

    /**
     * Add a condition
     *
     * @param condition
     */
    public void addCondition(ConditionAbstractType condition) {
        this.conditions.add(condition);
    }

    /**
     * Remove a condition
     *
     * @param condition
     */
    public void removeCondition(ConditionAbstractType condition) {
        this.conditions.remove(condition);
    }

    /**
     * Gets an read only conditions list.
     */
    public List<ConditionAbstractType> getConditions() {
        return Collections.unmodifiableList(this.conditions);
    }
}