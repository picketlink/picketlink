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
package org.picketlink.test.permission;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.picketlink.Identity;
import org.picketlink.annotations.PicketLink;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PermissionManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.basic.User;
import org.picketlink.test.AbstractArquillianTestCase;
import org.picketlink.test.AbstractJPADeploymentTestCase;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.picketlink.idm.model.basic.BasicModel.getUser;

/**
 * @author Pedro Igor
 */
public class PermissionTestCase extends AbstractJPADeploymentTestCase {

    @Inject
    private Identity identity;

    @Inject
    private DefaultLoginCredentials credentials;

    @Inject
    private IdentityManager identityManager;

    @Inject
    private PermissionManager permissionManager;

    @Inject
    @PicketLink
    private EntityManager entityManager;

    @Inject
    private UserTransaction userTransaction;

    @Deployment (name="permission-support")
    public static WebArchive deployPermissionSupport() {
        return deploy("/META-INF/persistence-with-permission.xml", GenericPermissionTypeEntity.class, SomeEntity.class, AbstractArquillianTestCase.class);
    }

    @Deployment (name="no-permission-support")
    public static WebArchive deployNoPermissionSupport() {
        return deploy("no_permission.war", "/META-INF/persistence.xml", AbstractArquillianTestCase.class);
    }

    @Before
    public void onSetup() throws HeuristicRollbackException, RollbackException, HeuristicMixedException, SystemException {
        User user = getUser(this.identityManager, "john");

        if (user == null) {
            user = new User("john");

            this.identityManager.add(user);

            Password password = new Password("mypasswd");

            this.identityManager.updateCredential(user, password);

            this.userTransaction.commit();
        }

        this.credentials.setUserId(user.getLoginName());
        this.credentials.setPassword("mypasswd");

        this.identity.login();
    }

    @Test
    @OperateOnDeployment("permission-support")
    public void testGrantAndRevokePermission() {
        Account user = this.identity.getAccount();

        assertNotNull(user);

        this.permissionManager.grantPermission(user, "somefile.txt", "read");
        this.permissionManager.grantPermission(user, User.class, "read");

        assertTrue(this.identity.hasPermission("somefile.txt", "read"));
        assertTrue(this.identity.hasPermission(User.class, "read"));

        this.permissionManager.revokePermission(user, User.class, "read");
        this.permissionManager.revokePermission(user, "somefile.txt", "read");

        assertFalse(this.identity.hasPermission(User.class, "read"));
        assertFalse(this.identity.hasPermission("somefile.txt", "read"));
    }

    @Test
    @OperateOnDeployment("permission-support")
    public void testGrantAndRevokeEntityPermission() {
        Account user = this.identity.getAccount();

        assertNotNull(user);

        SomeEntity entity = new SomeEntity();

        this.entityManager.persist(entity);

        SomeEntity entity2 = new SomeEntity();

        this.entityManager.persist(entity2);

        this.permissionManager.grantPermission(user, entity, "load");

        assertTrue(this.identity.hasPermission(SomeEntity.class, entity.getId(), "load"));
        assertFalse(this.identity.hasPermission(SomeEntity.class, entity2.getId(), "load"));

        permissionManager.revokePermission(user, entity, "load");
        permissionManager.grantPermission(user, entity2, "load");

        assertFalse(this.identity.hasPermission(SomeEntity.class, entity.getId(), "load"));
        assertTrue(this.identity.hasPermission(SomeEntity.class, entity2.getId(), "load"));
    }

    @Test(expected = IdentityManagementException.class)
    @OperateOnDeployment("no-permission-support")
    public void failGrantPermission() {
        Account user = this.identity.getAccount();

        assertNotNull(user);

        this.permissionManager.grantPermission(user, "somefile.txt", "read");
    }

}
