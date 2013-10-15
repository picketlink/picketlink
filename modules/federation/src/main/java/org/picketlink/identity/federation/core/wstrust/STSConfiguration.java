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

import org.picketlink.identity.federation.core.sts.STSCoreConfig;

/**
 * <p>
 * The {@code STSConfiguration} interface allows access to the security token service (STS) configuration attributes.
 * </p>
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 * @author Anil.Saldhana@redhat.com
 */
public interface STSConfiguration extends STSCoreConfig {

    /**
     * <p>
     * Obtains the WS-Trust request handler class.
     * </p>
     *
     * @return a reference to the configured {@code WSTrustRequestHandler}.
     */
    WSTrustRequestHandler getRequestHandler();

    /**
     * <p>
     * Obtains the {@code ClaimsProcessor} that must be used to handle claims of the specified dialect.
     * </p>
     *
     * @param claimsDialect a {@code String} representing the claims dialect (usually a URL).
     *
     * @return the {@code ClaimsProcessor} to be used, or {@code null} if no processor could be found for the dialect.
     */
    ClaimsProcessor getClaimsProcessor(String claimsDialect);

    /**
     * <p>
     * Returns the configured canonicalization method.
     * </p>
     * <p>
     * <b>NOTE:</b> Defaults to javax.xml.crypto.dsig.CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS
     * </p>
     *
     * @return
     */
    String getXMLDSigCanonicalizationMethod();
}