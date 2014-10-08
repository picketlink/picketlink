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
package org.picketlink.test.idm.identitymodel.saas;

import org.junit.After;
import org.junit.Before;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.internal.DefaultPartitionManager;
import org.picketlink.idm.jpa.internal.JPAIdentityStore;
import org.picketlink.idm.spi.ContextInitializer;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.test.idm.identitymodel.saas.model.Application;
import org.picketlink.test.idm.identitymodel.saas.model.ApplicationAccess;
import org.picketlink.test.idm.identitymodel.saas.model.ApplicationRealm;
import org.picketlink.test.idm.identitymodel.saas.model.Grant;
import org.picketlink.test.idm.identitymodel.saas.model.Group;
import org.picketlink.test.idm.identitymodel.saas.model.GroupMembership;
import org.picketlink.test.idm.identitymodel.saas.model.Realm;
import org.picketlink.test.idm.identitymodel.saas.model.Role;
import org.picketlink.test.idm.identitymodel.saas.model.User;
import org.picketlink.test.idm.identitymodel.saas.model.entity.ApplicationAccessTypeEntity;
import org.picketlink.test.idm.identitymodel.saas.model.entity.ApplicationRealmTypeEntity;
import org.picketlink.test.idm.identitymodel.saas.model.entity.ApplicationTypeEntity;
import org.picketlink.test.idm.identitymodel.saas.model.entity.GrantTypeEntity;
import org.picketlink.test.idm.identitymodel.saas.model.entity.GroupMembershipTypeEntity;
import org.picketlink.test.idm.identitymodel.saas.model.entity.GroupTypeEntity;
import org.picketlink.test.idm.identitymodel.saas.model.entity.PartitionTypeEntity;
import org.picketlink.test.idm.identitymodel.saas.model.entity.PasswordCredentialTypeEntity;
import org.picketlink.test.idm.identitymodel.saas.model.entity.RealmTypeEntity;
import org.picketlink.test.idm.identitymodel.saas.model.entity.RelationshipIdentityTypeEntity;
import org.picketlink.test.idm.identitymodel.saas.model.entity.RelationshipTypeEntity;
import org.picketlink.test.idm.identitymodel.saas.model.entity.RoleTypeEntity;
import org.picketlink.test.idm.identitymodel.saas.model.entity.UserTypeEntity;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

/**
 * <p>A base class of tests.</p>
 *
 * @author Pedro Igor
 */
public abstract class AbstractIdentityManagementTestCase {

    protected static final String REALM_ACME_NAME = "Acme";
    protected static final String APPLICATION_SALES_NAME = "Sales Application";

    private PartitionManager partitionManager;
    private Realm acmeRealm;
    private Application salesApplication;
    private ApplicationRealm salesApplicationPartition;
    private EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;

    @Before
    public void onBefore() throws Exception {
        initializeEntityManager();

        createPartitionManager();
        createDefaultRealm();
        createSalesApplication();
    }

    @After
    public void onAfter() throws Exception {
        closeEntityManager();
    }

    protected PartitionManager getPartitionManager() {
        return this.partitionManager;
    }

    protected Realm getAcmeRealm() {
        return this.acmeRealm;
    }

    protected Application getSalesApplication() {
        return this.salesApplication;
    }

    protected ApplicationRealm getSalesApplicationPartition() {
        return this.salesApplicationPartition;
    }

    private void createPartitionManager() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named("default.config")
                .stores()
                    .jpa()
                        .supportType(
                            User.class,
                            Role.class,
                            Group.class,
                            Realm.class,
                            Application.class,
                            ApplicationRealm.class)
                        .supportGlobalRelationship(
                            Grant.class,
                            GroupMembership.class,
                            ApplicationAccess.class)
                        .supportCredentials(true)
                        .mappedEntity(
                            ApplicationAccessTypeEntity.class,
                            ApplicationTypeEntity.class,
                            ApplicationRealmTypeEntity.class,
                            PartitionTypeEntity.class,
                            GrantTypeEntity.class,
                            GroupMembershipTypeEntity.class,
                            GroupTypeEntity.class,
                            RealmTypeEntity.class,
                            RoleTypeEntity.class,
                            UserTypeEntity.class,
                            PasswordCredentialTypeEntity.class,
                            RelationshipTypeEntity.class,
                            RelationshipIdentityTypeEntity.class)
                        .addContextInitializer(new ContextInitializer() {
                            @Override
                            public void initContextForStore(IdentityContext context, IdentityStore<?> store) {
                                if (store instanceof JPAIdentityStore) {
                                    if (!context.isParameterSet(JPAIdentityStore.INVOCATION_CTX_ENTITY_MANAGER)) {
                                        context.setParameter(JPAIdentityStore.INVOCATION_CTX_ENTITY_MANAGER, entityManager);
                                    }
                                }
                            }
                        });

        this.partitionManager = new DefaultPartitionManager(builder.buildAll());
    }

    private void createSalesApplication() {
        this.salesApplication = new Application(APPLICATION_SALES_NAME);

        IdentityManager identityManager = this.partitionManager.createIdentityManager(this.acmeRealm);

        identityManager.add(salesApplication);

        this.salesApplicationPartition = new ApplicationRealm(APPLICATION_SALES_NAME);

        this.partitionManager.add(this.salesApplicationPartition);
    }

    private void createDefaultRealm() throws NoSuchAlgorithmException {
        this.acmeRealm = new Realm(REALM_ACME_NAME);

        this.acmeRealm.setEnforceSSL(true);

        KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();

        this.acmeRealm.setPrivateKey(keyPair.getPrivate().getEncoded());
        this.acmeRealm.setPublickKey(keyPair.getPublic().getEncoded());

        this.acmeRealm.setNumberFailedLoginAttempts(3);

        this.partitionManager.add(this.acmeRealm);
    }

    private void initializeEntityManager() {
        this.entityManagerFactory = Persistence.createEntityManagerFactory("picketlink-saas-identity-model-pu");
        this.entityManager = entityManagerFactory.createEntityManager();
        this.entityManager.getTransaction().begin();
    }

    private void closeEntityManager() {
        this.entityManager.flush();
        this.entityManager.getTransaction().commit();
        this.entityManager.close();
        this.entityManagerFactory.close();
    }

}
