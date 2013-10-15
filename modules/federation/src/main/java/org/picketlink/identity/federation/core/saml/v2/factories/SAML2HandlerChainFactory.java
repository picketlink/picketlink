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
package org.picketlink.identity.federation.core.saml.v2.factories;

import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerChain;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerChain;

/**
 * Creates {@code SAML2HandlerChain}
 *
 * @author Anil.Saldhana@redhat.com
 * @since Nov 6, 2009
 */
public class SAML2HandlerChainFactory {

    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    public static SAML2HandlerChain createChain() {
        return new DefaultSAML2HandlerChain();
    }

    public static SAML2HandlerChain createChain(String fqn) throws ProcessingException {
        if (fqn == null)
            throw logger.nullArgumentError("fqn");

        Class<?> clazz = SecurityActions.loadClass(SAML2HandlerChainFactory.class, fqn);
        if (clazz == null)
            throw logger.classNotLoadedError(fqn);

        try {
            return (SAML2HandlerChain) clazz.newInstance();
        } catch (Exception e) {
            throw logger.couldNotCreateInstance(fqn, e);
        }
    }
}