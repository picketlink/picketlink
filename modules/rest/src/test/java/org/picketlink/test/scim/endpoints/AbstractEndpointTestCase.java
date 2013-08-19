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

import java.io.Serializable;
import java.net.URL;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.internal.DefaultPartitionManager;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;
import org.picketlink.scim.PicketLinkSCIMApplication;
import org.picketlink.test.scim.EmbeddedWebServerBase;

/**
 * Base class for the SCIM Endpoint tests
 *
 * @author anil saldhana
 * @since Apr 17, 2013
 */
public abstract class AbstractEndpointTestCase extends EmbeddedWebServerBase {

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
//                        .identityClass(IdentityObject.class)
//                        .attributeClass(IdentityObjectAttribute.class)
//                        .relationshipClass(RelationshipObject.class)
//                        .relationshipIdentityClass(RelationshipIdentityObject.class)
//                        .relationshipAttributeClass(RelationshipObjectAttribute.class)
//                        .credentialClass(CredentialObject.class)
//                        .credentialAttributeClass(CredentialObjectAttribute.class)
//                        .partitionClass(PartitionObject.class)
//                        .supportAllFeatures()
//                        .addContextInitializer(new JPAContextInitializer(entityManagerFactory) {
//                            @Override
//                            public EntityManager getEntityManager() {
//                                return entityManager;
//                            }})
                        ;


        PartitionManager partitionManager = new DefaultPartitionManager(builder.build());

        partitionManager.add(new Realm(Realm.DEFAULT_REALM));

        // FIXME: IdentityManager is not threadsafe
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
            group.setAttribute(new Attribute<String>("ID", "jboss"));
            identityManager.add(group);
        }

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

        /*
         * context.setConfigurations(new Configuration[]{new WebXmlConfiguration(),new WebInfConfiguration(), new
         * EnvConfiguration(), new PlusConfiguration(), new JettyWebXmlConfiguration()});
         */

        context.setContextPath("/");

        ServletHolder servletHolder = new ServletHolder(new HttpServletDispatcher());
        servletHolder.setInitParameter("javax.ws.rs.Application", PicketLinkSCIMApplication.class.getName());
        context.addServlet(servletHolder, "/*");

        server.setHandler(context);
    }
}