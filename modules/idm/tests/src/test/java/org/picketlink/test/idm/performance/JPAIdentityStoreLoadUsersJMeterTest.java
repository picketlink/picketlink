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

package org.picketlink.test.idm.performance;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.internal.DefaultPartitionManager;
import org.picketlink.idm.jpa.model.sample.simple.AccountTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.AttributeTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.DigestCredentialTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.GroupTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.IdentityTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.OTPCredentialTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.PartitionTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.PasswordCredentialTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.RelationshipIdentityTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.RelationshipTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.RoleTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.X509CredentialTypeEntity;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;
import org.picketlink.test.idm.basic.MyCustomAccountEntity;
import org.picketlink.test.idm.partition.CustomPartitionEntity;
import org.picketlink.test.idm.relationship.CustomRelationshipTypeEntity;
import org.picketlink.test.idm.util.JPAContextInitializer;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * @author Pedro Silva
 *
 */
public class JPAIdentityStoreLoadUsersJMeterTest extends AbstractJavaSamplerClient {

    private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpa-identity-store-tests-pu");
    private static PartitionManager partitionManager = null;
    private static final ThreadLocal<EntityManager> entityManager = new ThreadLocal<EntityManager>();

    static {
        initializeEntityManager();

        partitionManager = createPartitionManager();

        closeEntityManager();
    }

    private static void closeEntityManager() {
        entityManager.get().getTransaction().commit();
        entityManager.get().close();
        entityManager.remove();
    }

    private static void initializeEntityManager() {
        entityManager.set(emf.createEntityManager());
        entityManager.get().getTransaction().begin();
    }

    @Override
    public Arguments getDefaultParameters() {
        Arguments arguments = new Arguments();

        arguments.addArgument("loginName", "Sample User");

        return arguments;
    }

    @Override
    public void setupTest(JavaSamplerContext context) {

    }

    @Override
    public void teardownTest(JavaSamplerContext context) {
    }

    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        SampleResult result = new SampleResult();

        result.sampleStart();

        boolean success = true;

        String loginName = context.getParameter("loginName");

        if (loginName == null) {
            loginName = "Sample User";
        }

        JMeterVariables vars = JMeterContextService.getContext().getVariables();

        vars.put("loginName", loginName);

        try {
            initializeEntityManager();

            User user = new User(loginName);

            IdentityManager identityManager = partitionManager.createIdentityManager();

            identityManager.add(user);

            Role role = new Role(loginName);

            identityManager.add(role);

            Group group = new Group(loginName);

            identityManager.add(group);

            RelationshipManager relationshipManager = partitionManager.createRelationshipManager();

            BasicModel.grantRole(relationshipManager, user, role);
            BasicModel.addToGroup(relationshipManager, user, group);

            for (int i = 0;i < 20;i++) {
                user.setAttribute(new Attribute("Attribute " + user.getLoginName() + i, "Value " + i));
            }

            identityManager.update(user);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            result.sampleEnd();
            result.setSuccessful(success);
            closeEntityManager();
        }

        return result;
    }

    private static PartitionManager createPartitionManager() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named("default")
                .stores()
                    .jpa()
                        .mappedEntity(
                                PartitionTypeEntity.class,
                                MyCustomAccountEntity.class,
                                RoleTypeEntity.class,
                                GroupTypeEntity.class,
                                IdentityTypeEntity.class,
                                CustomRelationshipTypeEntity.class,
                                CustomPartitionEntity.class,
                                RelationshipTypeEntity.class,
                                RelationshipIdentityTypeEntity.class,
                                PasswordCredentialTypeEntity.class,
                                DigestCredentialTypeEntity.class,
                                X509CredentialTypeEntity.class,
                                OTPCredentialTypeEntity.class,
                                AttributeTypeEntity.class,
                                AccountTypeEntity.class
                        )
                        .supportGlobalRelationship(org.picketlink.idm.model.Relationship.class)
                        .addContextInitializer(new JPAContextInitializer(null) {
                            @Override
                            public EntityManager getEntityManager() {
                                return entityManager.get();
                            }
                        })
                        .supportAllFeatures();

            DefaultPartitionManager partitionManager = new DefaultPartitionManager(builder.buildAll());

            if (partitionManager.getPartition(Realm.class, Realm.DEFAULT_REALM) == null) {
                partitionManager.add(new Realm(Realm.DEFAULT_REALM));
            }

            return partitionManager;
        }

}
