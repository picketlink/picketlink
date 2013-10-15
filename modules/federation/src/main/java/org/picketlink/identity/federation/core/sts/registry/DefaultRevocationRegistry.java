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

import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * A simple {@code RevocationRegistry} that keeps the revoked token ids in a memory-only cache. This registry is only
 * used if no
 * other implementation has been configured and it doesn't persist the revoked ids. For these reasons it is highly
 * recommended
 * that this implementation be used only in testing scenarios.
 * </p>
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class DefaultRevocationRegistry implements RevocationRegistry {

    private static Set<String> ids = new HashSet<String>();

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.identity.federation.core.wstrust.plugins.RevocationRegistry#isRevoked(java.lang.String,
     * java.lang.String)
     */
    public boolean isRevoked(String tokenType, String id) {
        return ids.contains(id);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.identity.federation.core.wstrust.plugins.RevocationRegistry#revokeToken(java.lang.String,
     * java.lang.String)
     */
    public void revokeToken(String tokenType, String id) {
        ids.add(id);
    }
}