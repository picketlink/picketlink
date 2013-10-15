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

package org.picketlink.identity.federation.core.wstrust.plugins.saml;

import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType;

import java.util.Map;

/**
 * <p>
 * An interface used by {@code SAML20TokenProvider} to retrieve an environment specific attribute that will be inserted
 * into the
 * Assertion.
 * </p>
 *
 * @author <a href="mailto:Babak@redhat.com">Babak Mozaffari</a>
 */
public interface SAML20TokenAttributeProvider {

    /**
     * Sets properties on the Attribute Provider that may affect its behavior
     *
     * @param properties A set of string properties, some or all of which might impact the provider's behavior
     */
    void setProperties(Map<String, String> properties);

    /**
     * Given the security context, environment or other static or non-static criteria, returns an attribute statement to
     * be
     * included in the SAML v2 Assertion
     *
     * @return An Attribute Statement to be inserted in the SAML v2 Assertion
     */
    AttributeStatementType getAttributeStatement();
}
