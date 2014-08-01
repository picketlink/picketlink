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
package org.picketlink.test.authorization;

import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.picketlink.Identity;
import org.picketlink.IdentityConfigurationEvent;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PermissionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.jpa.model.sample.simple.AccountTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.AttributeTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.GroupTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.IdentityTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.PartitionTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.PasswordCredentialTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.RelationshipIdentityTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.RelationshipTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.RoleTypeEntity;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.basic.Grant;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.GroupMembership;
import org.picketlink.idm.model.basic.GroupRole;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;
import org.picketlink.internal.EntityManagerContextInitializer;
import org.picketlink.test.AbstractJPADeploymentTestCase;
import org.picketlink.test.authorization.permission.GenericPermissionTypeEntity;
import org.picketlink.test.authorization.permission.SomeEntity;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.UserTransaction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Pedro Igor
 */
public abstract class AbstractAuthorizationTestCase extends AbstractJPADeploymentTestCase {

    public static final String USER_NAME = "john";
    public static final String USER_PASSWORD = "password";

    @Inject
    protected IdentityManager identityManager;

    @Inject
    protected RelationshipManager relationshipManager;

    @Inject
    protected PermissionManager permissionManager;

    @Inject
    protected Identity identity;

    @Inject
    protected DefaultLoginCredentials credentials;

    @Inject
    protected UserTransaction userTransaction;

    public static WebArchive create(Class<?>... classes) {
        List<Class<?>> classesToAdd = new ArrayList(Arrays.asList(classes));

        classesToAdd.add(GenericPermissionTypeEntity.class);
        classesToAdd.add(AbstractAuthorizationTestCase.class);
        classesToAdd.add(MyCustomRole.class);
        classesToAdd.add(MyCustomGroup.class);
        classesToAdd.add(MyCustomGrant.class);
        classesToAdd.add(MyCustomGroupMembership.class);
        classesToAdd.add(SomeEntity.class);
        classesToAdd.add(MyCustomRoleTypeEntity.class);
        classesToAdd.add(MyCustomGroupTypeEntity.class);

        return deploy("/META-INF/persistence-with-permission.xml", classesToAdd.toArray(new Class<?>[classesToAdd.size()]));

    }

    protected void performAuthentication() {
        DefaultLoginCredentials credentials = this.credentials;

        credentials.setPassword(USER_PASSWORD);
        credentials.setUserId(USER_NAME);

        Identity identity = this.identity;

        Identity.AuthenticationResult status = identity.login();

        assertEquals(Identity.AuthenticationResult.SUCCESS, status);
        assertTrue(identity.isLoggedIn());

        assertEquals(this.identity.getAccount(), identity.getAccount());
    }

    @ApplicationScoped
    public static class IDMConfiguration {

        @Inject
        private EntityManagerContextInitializer contextInitializer;

        public void observeIdentityConfigurationEvent(@Observes IdentityConfigurationEvent event) throws Exception {
            IdentityConfigurationBuilder builder = event.getConfig();

            builder
                .named("custom-config")
                    .stores()
                        .jpa()
                            .mappedEntity(
                                AccountTypeEntity.class,
                                RoleTypeEntity.class,
                                GroupTypeEntity.class,
                                MyCustomRoleTypeEntity.class,
                                MyCustomGroupTypeEntity.class,
                                IdentityTypeEntity.class,
                                RelationshipTypeEntity.class,
                                RelationshipIdentityTypeEntity.class,
                                PartitionTypeEntity.class,
                                PasswordCredentialTypeEntity.class,
                                GenericPermissionTypeEntity.class,
                                AttributeTypeEntity.class)
                            .addContextInitializer(this.contextInitializer)
                            .supportType(MyCustomRole.class,
                                MyCustomGroup.class,
                                User.class,
                                Partition.class,
                                Role.class,
                                Group.class)
                            .supportGlobalRelationship(
                                MyCustomGrant.class,
                                MyCustomGroupMembership.class,
                                Grant.class,
                                GroupMembership.class,
                                GroupRole.class)
                            .supportCredentials(true)
                            .supportPermissions(true)
                            .supportAttributes(true);
        }
    }
}
