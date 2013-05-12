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

import java.net.URL;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.builder.IdentityConfigurationBuilder;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.internal.DefaultIdentityManagerFactory;
import org.picketlink.idm.jpa.internal.JPAContextInitializer;
import org.picketlink.idm.jpa.schema.CredentialObject;
import org.picketlink.idm.jpa.schema.CredentialObjectAttribute;
import org.picketlink.idm.jpa.schema.IdentityObject;
import org.picketlink.idm.jpa.schema.IdentityObjectAttribute;
import org.picketlink.idm.jpa.schema.PartitionObject;
import org.picketlink.idm.jpa.schema.RelationshipIdentityObject;
import org.picketlink.idm.jpa.schema.RelationshipObject;
import org.picketlink.idm.jpa.schema.RelationshipObjectAttribute;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;
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
            .stores()
                .jpa()
                    .addRealm(Realm.DEFAULT_REALM)
                    .identityClass(IdentityObject.class)
                    .attributeClass(IdentityObjectAttribute.class)
                    .relationshipClass(RelationshipObject.class)
                    .relationshipIdentityClass(RelationshipIdentityObject.class)
                    .relationshipAttributeClass(RelationshipObjectAttribute.class)
                    .credentialClass(CredentialObject.class)
                    .credentialAttributeClass(CredentialObjectAttribute.class)
                    .partitionClass(PartitionObject.class)
                    .supportAllFeatures()
                    .addContextInitializer(new JPAContextInitializer(entityManagerFactory) {
                        @Override
                        public EntityManager getEntityManager() {
                            return entityManager;
                        }
                    });

        // FIXME: IdentityManager is not threadsafe
        IdentityManager  identityManager = new DefaultIdentityManagerFactory(builder.build()).createIdentityManager();

        User anil = identityManager.getUser("anil");

        // Check when tests are running in unforked JVM
        if (anil == null) {
            SimpleUser admin = new SimpleUser("anil");
            admin.setAttribute(new Attribute<String>("ID", "1234"));
            admin.setEmail("admin@acme.com");

            identityManager.add(admin);
            identityManager.updateCredential(admin, new Password("tough"));

            Role roleAdmin = new SimpleRole("administrator");
            identityManager.add(roleAdmin);

            identityManager.grantRole(admin, roleAdmin);

            SimpleGroup group = new SimpleGroup("SomeGroup");
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