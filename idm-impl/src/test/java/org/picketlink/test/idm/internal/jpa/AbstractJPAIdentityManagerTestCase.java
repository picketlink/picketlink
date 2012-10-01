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

package org.picketlink.test.idm.internal.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.internal.DefaultIdentityManager;
import org.picketlink.idm.internal.JPAIdentityStore;
import org.picketlink.idm.internal.jpa.JPATemplate;
import org.picketlink.idm.spi.IdentityStore;

/**
 * <p>
 * Base class for testing the {@link JPAIdentityStore}.
 * </p>
 * <p>
 * This class creates a shared {@link EntityManagerFactory} and database instance. For each test method an {@link EntityManager}
 * instance is created.
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public abstract class AbstractJPAIdentityManagerTestCase {

    protected static EntityManagerFactory emf;

    protected EntityManager entityManager;

    private DefaultIdentityManager identityManager;

    /**
     * <p>
     * Creates a shared {@link EntityManagerFactory} and database instances
     * </p>
     * 
     * @throws Exception
     */
    @BeforeClass
    public static void onBeforeTests() throws Exception {
        emf = Persistence.createEntityManagerFactory("jpa-identity-store-tests-pu");
    }

    /**
     * <p>
     * Closes the shared {@link EntityManagerFactory} instance.
     * </p>
     * 
     * @throws Exception
     */
    @AfterClass
    public static void onAfterTests() throws Exception {
        emf.close();
    }

    /**
     * <p>
     * Creates an {@link EntityManager} instance for each test and begins a transaction
     * </p>
     * 
     * @throws Exception
     */
    @Before
    public void onSetupTest() throws Exception {
        this.entityManager = emf.createEntityManager();
        this.entityManager.getTransaction().begin();
    }

    /**
     * <p>
     * Flush/Closes the {@link EntityManager} and commit the current transaction.
     * </p>
     * 
     * @throws Exception
     */
    @After
    public void onFinishTest() throws Exception {
        this.entityManager.flush();
        this.entityManager.getTransaction().commit();
        this.entityManager.close();
    }

    protected IdentityManager getIdentityManager() {
        if (this.identityManager == null) {
            this.identityManager = new DefaultIdentityManager();
            this.identityManager.setIdentityStore(createIdentityStore());
        }

        return this.identityManager;
    }

    /**
     * <p>
     * Creates a new {@link JPAIdentityStore}
     * </p>
     * 
     * @return
     */
    protected IdentityStore createIdentityStore() {
        JPAIdentityStore identityStore = new JPAIdentityStore();

        JPATemplate jpaTemplate = new JPATemplate();

        jpaTemplate.setEntityManager(this.entityManager);

        identityStore.setJpaTemplate(jpaTemplate);

        return identityStore;
    }

}
