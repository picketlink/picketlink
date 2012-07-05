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

package org.picketlink.identity.federation.core.sts.registry;

import java.io.IOException;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

/**
 * <p>
 * {@link SecurityTokenRegistry} implementation that uses JPA to store tokens. By default, the JPA configuration has the name
 * {@code picketlink-sts} but a different configuration name can be specified through the constructor that takes a
 * {@code String} as a parameter.
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
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
     * 
     */
    private static interface TransactionCallback {

        /**
         * <p>
         * Executes some logic given the {@link EntityManager} instance.
         * </p>
         * 
         * @param entityManager
         */
        public void executeInTransaction(EntityManager entityManager);

    }
}