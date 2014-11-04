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
package org.picketlink.test.scim.endpoints;

import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.internal.DefaultPartitionManager;
import org.picketlink.idm.jpa.internal.JPAIdentityStore;
import org.picketlink.idm.jpa.model.sample.simple.AccountTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.AttributeTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.GroupTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.IdentityTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.PartitionTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.PasswordCredentialTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.RelationshipIdentityTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.RelationshipTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.RoleTypeEntity;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.spi.ContextInitializer;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.scim.endpoints.PicketLinkSCIMApplication;
import org.picketlink.test.scim.EmbeddedWebServerBase;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.Serializable;
import java.net.URL;
import java.util.List;

/**
 * Base class for the SCIM Endpoint tests
 *
 * @author anil saldhana
 * @since Apr 17, 2013
 */
public abstract class AbstractEndpointTestCase extends EmbeddedWebServerBase {
    protected String storedUserId = null;
    protected String storedGroupId = null;

    protected void populateIDM() {
        if (Thread.currentThread().getContextClassLoader() == null) {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader()); // Hibernate EntityManager issue
        }
        // Use JPA
        final EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("picketlink-scim-pu");

        final EntityManager entityManager = entityManagerFactory.createEntityManager();

        entityManager.getTransaction().begin();

        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();


        builder
                .named("default")
                .stores()
                .jpa()
                .mappedEntity(
                        AccountTypeEntity.class,
                        RoleTypeEntity.class,
                        GroupTypeEntity.class,
                        IdentityTypeEntity.class,
                        RelationshipTypeEntity.class,
                        RelationshipIdentityTypeEntity.class,
                        PartitionTypeEntity.class,
                        PasswordCredentialTypeEntity.class,
                        AttributeTypeEntity.class)
                .supportGlobalRelationship(Relationship.class).addContextInitializer(new ContextInitializer() {
                   @Override
                   public void initContextForStore(IdentityContext context, IdentityStore<?> store) {
                      context.setParameter(JPAIdentityStore.INVOCATION_CTX_ENTITY_MANAGER, entityManager);
                   }
                 })
                 // Specify that this identity store configuration supports all features
                .supportAllFeatures();

        IdentityConfiguration identityConfig = builder.build();

        PartitionManager partitionManager = new DefaultPartitionManager(identityConfig);

        List<Realm> partitions = partitionManager.getPartitions(Realm.class);

        if(partitions != null){
            for(Partition partition: partitions){
                if(partition.getName().equalsIgnoreCase(Realm.DEFAULT_REALM)){
                    partitionManager.remove(partition);
                }
            }
        }
        partitionManager.add(new Realm(Realm.DEFAULT_REALM));

        IdentityManager  identityManager = partitionManager.createIdentityManager();

        User anil = BasicModel.getUser(identityManager, "anil");

        // Check when tests are running in unforked JVM
        if (anil == null) {
            User admin = new User("anil");
            admin.setAttribute(new Attribute<Serializable>("ID", "1234"));
            admin.setEmail("admin@acme.com");

            identityManager.add(admin);
            identityManager.updateCredential(admin, new Password("tough"));

            Role roleAdmin = new Role("administrator");
            identityManager.add(roleAdmin);

            RelationshipManager relationshipManager = partitionManager.createRelationshipManager();

            BasicModel.grantRole(relationshipManager, admin, roleAdmin);

            Group group = new Group("SomeGroup");
            group.setAttribute(new Attribute<String>("ID", "jboss_groupid"));
            identityManager.add(group);

            anil = BasicModel.getUser(identityManager,"anil");
        }

        Group storedGroup = BasicModel.getGroup(identityManager,"SomeGroup");
        storedGroupId = storedGroup.getId();

        storedUserId = anil.getId();


        entityManager.getTransaction().commit();
        entityManager.close();
    }

    @Override
    protected void establishUserApps() {
        populateIDM();

        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        if (tcl == null) {
            tcl = getClass().getClassLoader();
        }

        final String WEBAPPDIR = "scim";

        final String CONTEXTPATH = "/*";

        // for localhost:port/admin/index.html and whatever else is in the webapp directory
        final URL warUrl = tcl.getResource(WEBAPPDIR);
        final String warUrlString = warUrl.toExternalForm();

        WebAppContext context = createWebApp(CONTEXTPATH, warUrlString);
        context.setClassLoader(getClass().getClassLoader());
        context.setExtraClasspath(warUrlString + "/..");

        context.setConfigurationClasses(new String[] { "org.eclipse.jetty.webapp.WebInfConfiguration",
                "org.eclipse.jetty.webapp.WebXmlConfiguration", "org.eclipse.jetty.webapp.MetaInfConfiguration",
                "org.eclipse.jetty.webapp.FragmentConfiguration", "org.eclipse.jetty.plus.webapp.EnvConfiguration",
                // "org.eclipse.jetty.plus.webapp.PlusConfiguration",
                "org.eclipse.jetty.webapp.JettyWebXmlConfiguration", "org.eclipse.jetty.webapp.TagLibConfiguration" });

        context.setContextPath("/");

        ServletHolder servletHolder = new ServletHolder(new HttpServletDispatcher());
        servletHolder.setInitParameter("javax.ws.rs.Application", PicketLinkSCIMApplication.class.getName());
        context.addServlet(servletHolder, "/*");

        server.setHandler(context);
    }
}