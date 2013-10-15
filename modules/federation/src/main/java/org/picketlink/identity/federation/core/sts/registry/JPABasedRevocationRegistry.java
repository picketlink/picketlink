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

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

/**
 * <p>
 * {@code JPABasedRevocationRegistry} is a revocation registry implementation that uses JPA to store the ids of the
 * revoked
 * (canceled) security tokens on a database. By default, the JPA configuration has the name {@code picketlink-sts} but
 * a
 * different configuration name can be specified through the constructor that takes a {@code String} as a parameter.
 * </p>
 * <p>
 * NOTE: this implementation doesn't keep any cache of the security token ids. It performs a JPA query every time the
 * {@code isRevoked(String id)} method is called. Many JPA providers have internal caching mechanisms that can keep the
 * data in
 * the cache synchronized with the database and avoid unnecessary trips to the database. This makes this registry a
 * good
 * choice
 * for clustered environments as any changes to the revocation table made by a node will be visible to the other nodes.
 * </p>
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class JPABasedRevocationRegistry extends AbstractJPARegistry implements RevocationRegistry {

    /**
     * <p>
     * Creates an instance of {@code JPABasedRevocationRegistry} that uses the default {@code picketlink-sts} JPA configuration
     * to persist the ids of the canceled security tokens.
     * </p>
     */
    public JPABasedRevocationRegistry() {
        super();
    }

    /**
     * <p>
     * Creates an instance of {@code JPABasedRevocationRegistry} that uses the specified JPA configuration to persist the ids of
     * the canceled security tokens.
     * </p>
     *
     * @param configuration a {@code String} representing the JPA configuration name to be used.
     */
    public JPABasedRevocationRegistry(String configuration) {
        super(configuration);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.identity.federation.core.wstrust.plugins.RevocationRegistry#isRevoked(java.lang.String,
     * java.lang.String)
     */
    public boolean isRevoked(String tokenType, String id) {
        // try to locate a RevokedToken entity with the specified id.
        EntityManager manager = getEntityManagerFactory().createEntityManager();
        Object object = manager.find(RevokedToken.class, id);
        manager.close();

        return object != null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.identity.federation.core.wstrust.plugins.RevocationRegistry#revokeToken(java.lang.String,
     * java.lang.String)
     */
    public void revokeToken(String tokenType, String id) {
        // if a RevokedToken entity with the specified id doesn't exist in the database, create one and insert it.
        EntityManager manager = getEntityManagerFactory().createEntityManager();
        if (manager.find(RevokedToken.class, id) != null) {
            logger.debug("Token with id=" + id + " has already been cancelled");
        } else {
            RevokedToken revokedToken = new RevokedToken(tokenType, id);
            EntityTransaction transaction = manager.getTransaction();
            transaction.begin();
            manager.persist(revokedToken);
            transaction.commit();
        }
        manager.close();
    }
}