/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.picketlink.idm.internal;

import org.picketlink.idm.DefaultIdentityCache;
import org.picketlink.idm.IdentityCache;
import org.picketlink.idm.credential.internal.DefaultCredentialHandlerFactory;
import org.picketlink.idm.credential.spi.CredentialHandlerFactory;
import org.picketlink.idm.event.EventBridge;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.IdentityStoreInvocationContext;
import org.picketlink.idm.spi.IdentityStoreInvocationContextFactory;

/**
 * A simple implementation of {@link IdentityStoreInvocationContextFactory} that has no dependencies on JPA
 * @author anil saldhana
 * @since Dec 13, 2012
 */
public class SimpleIdentityStoreInvocationContextFactory implements IdentityStoreInvocationContextFactory {
    private EventBridge eventBridge;
    private CredentialHandlerFactory credentialHandlerFactory = new DefaultCredentialHandlerFactory();
    private IdentityCache identityCache = new DefaultIdentityCache();
    
    @Override
    public IdentityStoreInvocationContext createContext() {
        return new IdentityStoreInvocationContext(this.identityCache, eventBridge, credentialHandlerFactory);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void initContextForStore(IdentityStoreInvocationContext ctx, IdentityStore store) {
    }
}
