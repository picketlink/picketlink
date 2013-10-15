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
package org.picketlink.identity.federation.core.sts.registry;

import org.picketlink.identity.federation.core.sts.PicketLinkCoreSTS;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hashmap based token registry
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jan 4, 2011
 */
public class DefaultTokenRegistry implements SecurityTokenRegistry {

    protected Map<String, Object> tokens = new ConcurrentHashMap<String, Object>();

    /**
     * @see org.picketlink.identity.federation.core.sts.registry.SecurityTokenRegistry#addToken(java.lang.String,
     *      java.lang.Object)
     */
    public void addToken(String tokenID, Object token) throws IOException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(PicketLinkCoreSTS.rte);

        tokens.put(tokenID, token);
    }

    /**
     * @see org.picketlink.identity.federation.core.sts.registry.SecurityTokenRegistry#getToken(java.lang.String)
     */
    public Object getToken(String tokenID) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(PicketLinkCoreSTS.rte);

        return tokens.get(tokenID);
    }

    /**
     * @see org.picketlink.identity.federation.core.sts.registry.SecurityTokenRegistry#removeToken(java.lang.String)
     */
    public void removeToken(String tokenID) throws IOException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(PicketLinkCoreSTS.rte);
        tokens.remove(tokenID);
    }
}