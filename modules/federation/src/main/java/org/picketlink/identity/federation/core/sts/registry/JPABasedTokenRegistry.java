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
import java.io.IOException;

/**
 * <p>
 * {@link SecurityTokenRegistry} implementation that uses JPA to store tokens. By default, the JPA configuration has
 * the
 * name
 * {@code picketlink-sts} but a different configuration name can be specified through the constructor that takes a
 * {@code String} as a parameter.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * @see {@link SecurityToken}
 */
public class JPABasedTokenRegistry extends AbstractJPARegistry implements SecurityTokenRegistry {

    public JPABasedTokenRegistry() {
        super();
    }

    public JPABasedTokenRegistry(String configuration) {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.identity.federation.core.sts.registry.SecurityTokenRegistry#addToken(java.lang.String,
     * java.lang.Object)
     */
    public void addToken(final String id, final Object token) throws IOException {
        executeInTransaction(new TransactionCallback() {

            @Override
            public void executeInTransaction(EntityManager entityManager) {
                if (entityManager.find(SecurityToken.class, id) != null) {
                    logger.samlSecurityTokenAlreadyPersisted(id);
                } else {
                    SecurityToken securityToken = new SecurityToken(id, token);

                    entityManager.persist(securityToken);
                }
            }
        });
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.identity.federation.core.sts.registry.SecurityTokenRegistry#removeToken(java.lang.String)
     */
    public void removeToken(final String id) throws IOException {
        executeInTransaction(new TransactionCallback() {

            @Override
            public void executeInTransaction(EntityManager entityManager) {
                SecurityToken securityToken = entityManager.find(SecurityToken.class, id);

                if (securityToken == null) {
                    logger.samlSecurityTokenNotFoundInRegistry(id);
                } else {
                    entityManager.remove(securityToken);
                }
            }
        });
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.identity.federation.core.sts.registry.SecurityTokenRegistry#getToken(java.lang.String)
     */
    public Object getToken(final String id) {
        SecurityToken token = getEntityManagerFactory().createEntityManager().find(SecurityToken.class, id);

        if (token != null) {
            return token.unmarshalToken();
        }

        logger.samlSecurityTokenNotFoundInRegistry(id);

        return null;
    }

    /**
     * <p>
     * This method expects a {@link TransactionCallback} to execute some logic inside a managed transaction.
     * Invokers do not have to care about managing the {@link EntityManager} and transaction life cycle.
     * </p>
     *
     * @param callback
     */
    private void executeInTransaction(TransactionCallback callback) {
        EntityManager manager = null;
        EntityTransaction transaction = null;

        try {
            manager = getEntityManagerFactory().createEntityManager();
            transaction = manager.getTransaction();

            transaction.begin();

            callback.executeInTransaction(manager);
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error executing transaction.", e);
        } finally {
            if (transaction != null && transaction.isActive()) {
                transaction.commit();
            }

            manager.close();
        }
    }

    /**
     * <p>
     * This interface should be used to execute some logic inside a managed {@link EntityManager} transaction.
     * </p>
     *
     * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
     */
    private interface TransactionCallback {

        /**
         * <p>
         * Executes some logic given the {@link EntityManager} instance.
         * </p>
         *
         * @param entityManager
         */
        void executeInTransaction(EntityManager entityManager);

    }
}