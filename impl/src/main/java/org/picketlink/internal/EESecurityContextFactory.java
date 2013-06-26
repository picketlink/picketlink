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

package org.picketlink.internal;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.picketlink.idm.IdGenerator;
import org.picketlink.idm.IdentityCache;
import org.picketlink.idm.credential.spi.CredentialHandlerFactory;
import org.picketlink.idm.internal.DefaultIdGenerator;
import org.picketlink.idm.internal.DefaultIdentityCache;
import org.picketlink.idm.internal.DefaultSecurityContextFactory;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.SecurityContextFactory;

/**
 * 
 * @author Shane Bryzak
 *
 */
@ApplicationScoped
public class EESecurityContextFactory extends DefaultSecurityContextFactory implements SecurityContextFactory {

    @Inject CDIEventBridge cdiEventBridge;

    private CredentialHandlerFactory credentialHandlerFactory;
    private IdentityCache identityCache;
    private IdGenerator idGenerator;

    public EESecurityContextFactory() {
        credentialHandlerFactory = new CredentialHandlerFactory();
        identityCache = new DefaultIdentityCache();
        idGenerator = new DefaultIdGenerator();
    }

    @Override
    public IdentityContext createContext(Partition partition) {
        return new IdentityContext(this.identityCache, cdiEventBridge, credentialHandlerFactory, idGenerator, partition);
    }

}
