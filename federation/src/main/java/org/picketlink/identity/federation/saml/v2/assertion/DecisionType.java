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

/**
 * <p>
 * Java class for DecisionType.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * <p>
 *
 * <pre>
 * &lt;simpleType name="DecisionType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Permit"/>
 *     &lt;enumeration value="Deny"/>
 *     &lt;enumeration value="Indeterminate"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 *
 */
public enum DecisionType {
    PERMIT("Permit"), DENY("Deny"), INDETERMINATE("Indeterminate");
    private final String value;

    DecisionType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DecisionType fromValue(String v) {
        for (DecisionType c : DecisionType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}