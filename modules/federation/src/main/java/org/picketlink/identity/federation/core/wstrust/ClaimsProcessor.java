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
package org.picketlink.identity.federation.core.wstrust;

import org.picketlink.common.exceptions.fed.WSTrustException;
import org.picketlink.identity.federation.ws.trust.ClaimsType;

import java.security.Principal;
import java.util.Map;

/**
 * <p>
 * A {@code ClaimsProcessor} implementation is responsible for parsing the WS-Trust claims according to the specified
 * claims
 * dialect and retrieving the attributes that correspond to the required claims. {@code ClaimsProcessor}s may use the
 * properties
 * specified in the configuration to perform its job (for instance, to connect to an external LDAP server or IDM system
 * when
 * retrieving the attributes).
 * </p>
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public interface ClaimsProcessor {

    /**
     * <p>
     * Initializes the {@code ClaimsProcessor} using the specified properties map.
     * </p>
     *
     * @param properties a {@code Map<String, String>} that contains the properties that have been configured for this
     * {@code ClaimsProcessor}.
     */
    void initialize(Map<String, String> properties);

    /**
     * <p>
     * Parses the specified claims according to the claims dialect and returns a {@code Map} of attributes that
     * correspond to
     * the required claims. Implementing classes may get the attributes from a local context or from an external system
     * (like an
     * LDAP server or IDM system).
     * </p>
     *
     * @param claims a reference to the {@code ClaimsType} instance that contains the claims that must be inserted into
     * generated tokens as attributes.
     * @param principal the {@code Principal} to which the claims refer.
     *
     * @return a {@code Map<String, Object>} of attributes that correspond to the required claims.
     *
     * @throws WSTrustException if an error occurs while processing the claims.
     */
    Map<String, Object> processClaims(ClaimsType claims, Principal principal) throws WSTrustException;
}
