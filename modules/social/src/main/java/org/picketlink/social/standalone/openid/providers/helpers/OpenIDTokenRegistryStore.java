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
package org.picketlink.social.standalone.openid.providers.helpers;

import java.io.IOException;

import org.openid4java.association.Association;
import org.openid4java.association.AssociationException;
import org.openid4java.server.InMemoryServerAssociationStore;
import org.openid4java.server.ServerAssociationStore;
import org.picketlink.identity.federation.core.sts.registry.DefaultTokenRegistry;
import org.picketlink.identity.federation.core.sts.registry.SecurityTokenRegistry;

/**
 * A {@code SecurityTokenRegistry} for OpenID that uses in memory registry
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jan 20, 2011
 */
public class OpenIDTokenRegistryStore extends DefaultTokenRegistry implements ServerAssociationStore, SecurityTokenRegistry {
    protected InMemoryServerAssociationStore store = new InMemoryServerAssociationStore();

    /**
     * @see org.openid4java.server.ServerAssociationStore#generate(java.lang.String, int)
     */
    public Association generate(String type, int expiryIn) throws AssociationException {
        Association association = store.generate(type, expiryIn);
        try {
            addToken(association.getHandle(), association);
        } catch (IOException e) {
            throw new AssociationException(e);
        }
        return association;
    }

    /**
     * @see org.openid4java.server.ServerAssociationStore#load(java.lang.String)
     */
    public Association load(String handle) {
        return (Association) getToken(handle);
    }

    /**
     * @see org.openid4java.server.ServerAssociationStore#remove(java.lang.String)
     */
    public void remove(String handle) {
        try {
            removeToken(handle);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}