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
package org.picketlink.identity.federation.core.interfaces;

import org.picketlink.common.exceptions.ProcessingException;

import javax.xml.namespace.QName;
import java.util.Map;


/**
 * <p>
 * This interface defines the methods that must be implemented by security token providers.
 * </p>
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public interface SecurityTokenProvider {

    /**
     * An enumeration that identifies the family to which the security token provider belongs
     */
    enum FAMILY_TYPE {
        SAML2, SAML11, WS_TRUST, OPENID, OAUTH, CUSTOM;
    }

    /**
     * <p>
     * Initializes the {@code SecurityTokenProvider} using the specified properties map.
     * </p>
     *
     * @param properties a {@code Map<String, String>} that contains the properties that have been configured for this
     * {@code SecurityTokenProvider}.
     */
    void initialize(Map<String, String> properties);

    /**
     * Specify whether this token provider supports a particular namespace
     *
     * @param namespace a string value representing a namespace
     *
     * @return
     */
    boolean supports(String namespace);

    /**
     * Token Type
     *
     * @return
     */
    String tokenType();

    /**
     * Provide an optional {@code QName} for configuration
     *
     * @return
     */
    QName getSupportedQName();

    /**
     * The family where this security token provider belongs
     *
     * @return
     *
     * @see {@code FAMILY_TYPE}
     */
    String family();

    /**
     * <p>
     * Generates a security token using the information contained in the specified request context and stores the
     * newly-created
     * token in the context itself.
     * </p>
     *
     * @param context the {@code ProtocolContext} to be used when generating the token.
     *
     * @throws WSTrustException if an error occurs while creating the security token.
     */
    void issueToken(ProtocolContext context) throws ProcessingException;

    /**
     * <p>
     * Renews the security token contained in the specified request context. This method is used when a previously
     * generated
     * token has expired, generating a new version of the same token with different expiration semantics.
     * </p>
     *
     * @param context the {@code ProtocolContext} that contains the token to be renewed.
     *
     * @throws WSTrustException if an error occurs while renewing the security token.
     */
    void renewToken(ProtocolContext context) throws ProcessingException;

    /**
     * <p>
     * Cancels the token contained in the specified request context. A security token is usually canceled when one wants
     * to make
     * sure that the token will not be used anymore. A security token can't be renewed once it has been canceled.
     * </p>
     *
     * @param context the {@code ProtocolContext} that contains the token to be canceled.
     *
     * @throws WSTrustException if an error occurs while canceling the security token.
     */
    void cancelToken(ProtocolContext context) throws ProcessingException;

    /**
     * <p>
     * Evaluates the validity of the token contained in the specified request context and sets the result in the context
     * itself.
     * The result can be a status, a new token, or both.
     * </p>
     *
     * @param context the {@code ProtocolContext} that contains the token to be validated.
     *
     * @throws WSTrustException if an error occurs while validating the security token.
     */
    void validateToken(ProtocolContext context) throws ProcessingException;
}