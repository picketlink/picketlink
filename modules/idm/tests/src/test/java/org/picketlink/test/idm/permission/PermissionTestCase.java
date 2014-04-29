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
package org.picketlink.test.idm.permission;

import org.junit.Test;
import org.picketlink.idm.PermissionManager;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.permission.IdentityPermission;
import org.picketlink.idm.permission.Permission;
import org.picketlink.test.idm.AbstractPartitionManagerTestCase;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.permission.entity.ProtectedEntity;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.JPAPermissionStoreConfigurationTester;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Pedro Igor
 */
@Configuration(include = JPAPermissionStoreConfigurationTester.class)
public class PermissionTestCase extends AbstractPartitionManagerTestCase {

    public PermissionTestCase(IdentityConfigurationTester visitor) {
        super(visitor);
    }

    @Test
    public void testGrantAndRevokeStringBasedPermission() {
        User bob = createUser("bob");
        PermissionManager permissionManager = getPermissionManager();

        // let's grant some permissions
        permissionManager.grantPermission(bob, "fileA.txt", "read");
        permissionManager.grantPermission(bob, "fileB.txt", "write");
        permissionManager.grantPermission(bob, "fileC.txt", "execute");
        permissionManager.grantPermission(bob, "fileD.txt", "read,write,execute");

        // all permissions should be granted
        assertTrue(hasPermission(bob, permissionManager.listPermissions("fileA.txt", "read")));
        assertTrue(hasPermission(bob, permissionManager.listPermissions("fileB.txt", "write")));
        assertTrue(hasPermission(bob, permissionManager.listPermissions("fileC.txt", "execute")));
        assertTrue(hasPermission(bob, permissionManager.listPermissions("fileD.txt", "read")));
        assertTrue(hasPermission(bob, permissionManager.listPermissions("fileD.txt", "write")));
        assertTrue(hasPermission(bob, permissionManager.listPermissions("fileD.txt", "execute")));

        // let's revoke a specific permission
        permissionManager.revokePermission(bob, "fileA.txt", "read");

        // let's test if only the permission above was actually revoked
        assertFalse(hasPermission(bob, permissionManager.listPermissions("FileA.txt", "read")));
        assertTrue(hasPermission(bob, permissionManager.listPermissions("fileB.txt", "write")));
        assertTrue(hasPermission(bob, permissionManager.listPermissions("fileC.txt", "execute")));
        assertTrue(hasPermission(bob, permissionManager.listPermissions("fileD.txt", "read")));
        assertTrue(hasPermission(bob, permissionManager.listPermissions("fileD.txt", "write")));
        assertTrue(hasPermission(bob, permissionManager.listPermissions("fileD.txt", "execute")));

        // let's revoke a single operation from a multi-operation permision
        permissionManager.revokePermission(bob, "fileD.txt", "read");

        // all operations except above should be granted
        assertFalse(hasPermission(bob, permissionManager.listPermissions("fileD.txt", "read")));
        assertTrue(hasPermission(bob, permissionManager.listPermissions("fileD.txt", "write")));
        assertTrue(hasPermission(bob, permissionManager.listPermissions("fileD.txt", "execute")));

        // let's clear all permissions for a specific resource
        permissionManager.clearPermissions("fileD.txt");

        // no permission should exist
        assertFalse(hasPermission(bob, permissionManager.listPermissions("fileD.txt", "write")));
        assertFalse(hasPermission(bob, permissionManager.listPermissions("fileD.txt", "execute")));
    }

    @Test
    public void testGrantAndRevokeClassBasedPermission() {
        User bob = createUser("bob");
        PermissionManager permissionManager = getPermissionManager();

        // let's grant some permissions
        permissionManager.grantPermission(bob, User.class, "create");

        // all permissions should be granted
        assertTrue(hasPermission(bob, permissionManager.listPermissions((Object) User.class, "create")));

        // let's revoke a specific permission
        permissionManager.revokePermission(bob, User.class, "create");

        // let's test if only the permission above was actually revoked
        assertFalse(hasPermission(bob, permissionManager.listPermissions((Object) User.class, "create")));

        permissionManager.grantPermission(bob, User.class, "create");

        assertTrue(hasPermission(bob, permissionManager.listPermissions((Object) User.class, "create")));

        permissionManager.clearPermissions(User.class);

        assertFalse(hasPermission(bob, permissionManager.listPermissions((Object) User.class, "create")));
    }

    @Test
    public void testGrantAndRevokeEntityBasedPermission() {
        EntityManager entityManager = getEntityManager();

        ProtectedEntity entity = new ProtectedEntity();

        entity.setName("Confidential");

        entityManager.persist(entity);

        User bob = createUser("bob");
        PermissionManager permissionManager = getPermissionManager();

        permissionManager.grantPermission(bob, entity, "load");

        assertTrue(hasPermission(bob, permissionManager.listPermissions(ProtectedEntity.class, entity.getId())));

        permissionManager.revokePermission(bob, entity, "load");

        assertFalse(hasPermission(bob, permissionManager.listPermissions(entity, "load")));
    }

    @Test
    public void testGrantAndRevokeMultipleUsers() {
        User bob = createUser("bob");
        User jane = createUser("jane");
        PermissionManager permissionManager = getPermissionManager();

        // let's grant some permissions
        permissionManager.grantPermission(bob, "fileA.txt", "read");
        permissionManager.grantPermission(jane, "fileB.txt", "write");

        assertTrue(hasPermission(bob, permissionManager.listPermissions("fileA.txt", "read")));
        assertTrue(hasPermission(jane, permissionManager.listPermissions("fileB.txt", "write")));

        assertFalse(hasPermission(jane, permissionManager.listPermissions("fileA.txt")));
        assertFalse(hasPermission(bob, permissionManager.listPermissions("fileB.txt")));
    }

    @Test
    public void testPermissionStorePartitioningByResourceClass() {
        User bob = createUser("bob");
        PermissionManager permissionManager = getPermissionManager();

        // let's grant some permissions
        permissionManager.grantPermission(bob, "fileA.txt", "read");

        EntityManager entityManager = getEntityManager();

        List result = entityManager.createQuery("from TypedPermissionTypeEntity").getResultList();

        assertEquals(0, result.size());

        assertTrue(hasPermission(bob, permissionManager.listPermissions("fileA.txt", "read")));

        ProtectedEntity entity = new ProtectedEntity();

        entity.setName("Confidential");

        entityManager.persist(entity);

        permissionManager.grantPermission(bob, entity, "load");

        result = entityManager.createQuery("from TypedPermissionTypeEntity").getResultList();

        assertEquals(1, result.size());

        result = entityManager.createQuery("from BasicPermissionTypeEntity").getResultList();

        assertEquals(1, result.size());
    }

    public boolean hasPermission(IdentityType identityType, List<Permission> permissions) {
        for (Permission permission : permissions) {
            if (IdentityPermission.class.isInstance(permission)) {
                IdentityPermission identityPermission = (IdentityPermission) permission;

                if (identityPermission.getAssignee().equals(identityType)) {
                    return true;
                }
            }
        }

        return false;
    }

    private EntityManager getEntityManager() {
        JPAPermissionStoreConfigurationTester visitor1 = (JPAPermissionStoreConfigurationTester) getVisitor();
        return visitor1.getEntityManager();
    }

    private PermissionManager getPermissionManager() {
        return getPartitionManager().createPermissionManager();
    }
}
