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
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PermissionManager;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.permission.IdentityPermission;
import org.picketlink.idm.permission.Permission;
import org.picketlink.test.idm.AbstractPartitionManagerTestCase;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.permission.entity.AllowedOperationTypeEntity;
import org.picketlink.test.idm.permission.entity.ProtectedEntity;
import org.picketlink.test.idm.testers.FileStoreConfigurationTester;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.JPAPermissionStoreConfigurationTester;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Pedro Igor
 */
@Configuration(include = {FileStoreConfigurationTester.class, JPAPermissionStoreConfigurationTester.class})
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
        permissionManager.revokePermission(bob, "fileB.txt", "write");

        // let's test if only the permission above was actually revoked
        assertFalse(hasPermission(bob, permissionManager.listPermissions("FileA.txt", "read")));
        assertFalse(hasPermission(bob, permissionManager.listPermissions("fileB.txt", "write")));
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

        assertTrue(hasPermission(bob, permissionManager.listPermissions("fileC.txt", "execute")));

        permissionManager.revokePermission(bob, "fileC.txt", "update,create,execute");

        List permissions = permissionManager.listPermissions(bob);

        assertTrue(permissions.isEmpty());
    }

    @Test
    public void testGrantAndRevokeClassBasedPermission() {
        User bob = createUser("bob");
        PermissionManager permissionManager = getPermissionManager();

        // let's grant some permissions
        permissionManager.grantPermission(bob, User.class, "read");
        permissionManager.grantPermission(bob, Role.class, "write");
        permissionManager.grantPermission(bob, Group.class, "execute");
        permissionManager.grantPermission(bob, Realm.class, "read,write,execute");

        // all permissions should be granted
        assertTrue(hasPermission(bob, permissionManager.listPermissions(User.class, "read")));
        assertTrue(hasPermission(bob, permissionManager.listPermissions(Role.class, "write")));
        assertTrue(hasPermission(bob, permissionManager.listPermissions(Group.class, "execute")));
        assertTrue(hasPermission(bob, permissionManager.listPermissions(Realm.class, "read")));
        assertTrue(hasPermission(bob, permissionManager.listPermissions(Realm.class, "write")));
        assertTrue(hasPermission(bob, permissionManager.listPermissions(Realm.class, "execute")));

        // let's revoke a specific permission
        permissionManager.revokePermission(bob, User.class, "read");
        permissionManager.revokePermission(bob, Role.class, "write");

        // let's test if only the permission above was actually revoked
        assertFalse(hasPermission(bob, permissionManager.listPermissions(User.class, "read")));
        assertFalse(hasPermission(bob, permissionManager.listPermissions(Role.class, "write")));
        assertTrue(hasPermission(bob, permissionManager.listPermissions(Group.class, "execute")));
        assertTrue(hasPermission(bob, permissionManager.listPermissions(Realm.class, "read")));
        assertTrue(hasPermission(bob, permissionManager.listPermissions(Realm.class, "write")));
        assertTrue(hasPermission(bob, permissionManager.listPermissions(Realm.class, "execute")));

        // let's revoke a single operation from a multi-operation permision
        permissionManager.revokePermission(bob, Realm.class, "read");

        // all operations except above should be granted
        assertFalse(hasPermission(bob, permissionManager.listPermissions(Realm.class, "read")));
        assertTrue(hasPermission(bob, permissionManager.listPermissions(Realm.class, "write")));
        assertTrue(hasPermission(bob, permissionManager.listPermissions(Realm.class, "execute")));

        // let's clear all permissions for a specific resource
        permissionManager.clearPermissions(Realm.class);

        // no permission should exist
        assertFalse(hasPermission(bob, permissionManager.listPermissions(Realm.class, "write")));
        assertFalse(hasPermission(bob, permissionManager.listPermissions(Realm.class, "execute")));

        assertTrue(hasPermission(bob, permissionManager.listPermissions(Group.class, "execute")));

        permissionManager.revokePermission(bob, Realm.class, "write,execute");

        List permissions = permissionManager.listPermissions(Realm.class);

        assertTrue(permissions.isEmpty());
    }

    @Test
    public void testGrantAndRevokeMultipleOperations() {
        User bob = createUser("bob");
        PermissionManager permissionManager = getPermissionManager();

        // let's grant some permissions
        permissionManager.grantPermission(bob, "fileA.txt", "read");

        // all permissions should be granted
        List<Permission> permissions = permissionManager.listPermissions("fileA.txt", "read");

        List<String> operations = new ArrayList<String>();

        for (Permission permission : permissions) {
            operations.add(permission.getOperation());
        }

        assertEquals(1, permissions.size());
        assertTrue(operations.contains("read"));

        permissionManager.grantPermission(bob, "fileA.txt", "read,write,execute");

        permissions = permissionManager.listPermissions("fileA.txt", "read,write,execute");
        operations = new ArrayList<String>();

        for (Permission permission : permissions) {
            operations.add(permission.getOperation());
        }

        assertEquals(3, permissions.size());
        assertTrue(operations.contains("read"));
        assertTrue(operations.contains("write"));
        assertTrue(operations.contains("execute"));

        permissions = permissionManager.listPermissions("fileA.txt");
        operations = new ArrayList<String>();

        for (Permission permission : permissions) {
            operations.add(permission.getOperation());
        }

        assertEquals(3, permissions.size());
        assertTrue(operations.contains("read"));
        assertTrue(operations.contains("write"));
        assertTrue(operations.contains("execute"));

        permissionManager.revokePermission(bob, "fileA.txt", "read,write,execute");

        permissions = permissionManager.listPermissions("fileA.txt");

        assertTrue(permissions.isEmpty());
    }

    @Test
    public void testListPermissionsByIdentityType() {
        User bob = createUser("bob");
        User jane = createUser("jane");
        PermissionManager permissionManager = getPermissionManager();

        // let's grant some permissions
        permissionManager.grantPermission(bob, "fileA.txt", "read");

        List<Permission> permissions = permissionManager.listPermissions(bob);

        List<String> operations = new ArrayList<String>();

        for (Permission permission : permissions) {
            operations.add(permission.getOperation());
        }

        assertEquals(1, permissions.size());
        assertTrue(operations.contains("read"));

        permissionManager.grantPermission(jane, "fileB.txt", "write,read,execute");

        permissions = permissionManager.listPermissions(jane);

        operations = new ArrayList<String>();

        for (Permission permission : permissions) {
            operations.add(permission.getOperation());
        }

        assertEquals(3, permissions.size());
        assertTrue(operations.contains("read"));
        assertTrue(operations.contains("write"));
        assertTrue(operations.contains("execute"));

        permissionManager.revokePermission(jane, "fileB.txt", "write,read,execute");
        permissions = permissionManager.listPermissions(jane);

        assertTrue(permissions.isEmpty());
    }

    @Test
    public void testPermissionProperties() {
        User bob = createUser("bob");
        PermissionManager permissionManager = getPermissionManager();

        // let's grant some permissions
        permissionManager.grantPermission(bob, "fileA.txt", "read");

        List<Permission> permissions = permissionManager.listPermissions("fileA.txt");

        assertEquals(1, permissions.size());

        Permission permission = permissions.get(0);

        assertEquals("fileA.txt", permission.getResource());
        assertEquals("read", permission.getOperation());

        permissionManager.grantPermission(bob, User.class, "create");

        permissions = permissionManager.listPermissions(User.class, "create");

        assertEquals(1, permissions.size());

        permission = permissions.get(0);

        assertEquals(User.class, permission.getResource());
        assertEquals("create", permission.getOperation());

    }

    @Test
    public void testRemoveOnIdentityTypeRemoval() {
        User bob = createUser("bob");
        PermissionManager permissionManager = getPermissionManager();

        // let's grant some permissions
        permissionManager.grantPermission(bob, "fileA.txt", "read");

        IdentityManager identityManager = getIdentityManager();

        identityManager.remove(bob);

        List<Permission> permissions = permissionManager.listPermissions(bob);

        assertTrue(permissions.isEmpty());
    }

    @Test
    public void testRevokeClassBasedPermission() {
        User bob = createUser("bob");
        PermissionManager permissionManager = getPermissionManager();

        // let's grant some permissions
        permissionManager.grantPermission(bob, Role.class, "read");

        IdentityManager identityManager = getIdentityManager();

        identityManager.remove(bob);

        List<Permission> permissions = permissionManager.listPermissions(bob);

        assertTrue(permissions.isEmpty());
    }

    @Test
    public void testGrantAndRevokeEntityBasedPermission() {
        ProtectedEntity entity = new ProtectedEntity();

        entity.setId(1l);
        entity.setName("Confidential");

        ProtectedEntity entity2 = new ProtectedEntity();

        entity2.setId(2l);
        entity2.setName("Confidential");

        User bob = createUser("bob");
        PermissionManager permissionManager = getPermissionManager();

        permissionManager.grantPermission(bob, entity, "load");

        assertTrue(hasPermission(bob, permissionManager.listPermissions(ProtectedEntity.class, entity.getId())));
        assertFalse(hasPermission(bob, permissionManager.listPermissions(ProtectedEntity.class, entity2.getId(), "load")));

        permissionManager.revokePermission(bob, entity, "load");
        permissionManager.grantPermission(bob, entity2, "load");

        assertFalse(hasPermission(bob, permissionManager.listPermissions(ProtectedEntity.class, entity.getId(), "load")));
        List<Permission> permissions = permissionManager.listPermissions(ProtectedEntity.class, entity2.getId(), "load");

        assertTrue(hasPermission(bob, permissions));

        Permission permission = permissions.get(0);

        assertEquals(ProtectedEntity.class, permission.getResourceClass());
        assertEquals(entity2.getId().toString(), permission.getResourceIdentifier());
        assertEquals("load", permission.getOperation());

        permissions = permissionManager.listPermissions(entity2, "load");

        assertTrue(hasPermission(bob, permissions));

        permission = permissions.get(0);

        assertEquals(entity2, permission.getResource());
        assertEquals("load", permission.getOperation());
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

        permissionManager.revokePermission(jane, "fileB.txt", "write");

        assertFalse(hasPermission(jane, permissionManager.listPermissions("fileB.txt")));
        assertTrue(hasPermission(bob, permissionManager.listPermissions("fileA.txt", "read")));
    }

    @Test
    @Configuration(exclude = FileStoreConfigurationTester.class)
    public void testPermissionStorePartitioningByResourceType() {
        User bob = createUser("bob");
        PermissionManager permissionManager = getPermissionManager();

        // let's grant some permissions
        permissionManager.grantPermission(bob, "fileA.txt", "read");

        EntityManager entityManager = getEntityManager();

        List result = entityManager.createQuery("from TypedPermissionTypeEntity p").getResultList();

        assertEquals(0, result.size());

        assertTrue(hasPermission(bob, permissionManager.listPermissions("fileA.txt", "read")));

        ProtectedEntity entity = new ProtectedEntity();

        entity.setName("Confidential");

        entityManager.persist(entity);

        permissionManager.grantPermission(bob, entity, "load");

        result = entityManager.createQuery("from TypedPermissionTypeEntity p").getResultList();

        assertEquals(1, result.size());

        result = entityManager.createQuery("from PermissionTypeEntity p").getResultList();

        assertEquals(1, result.size());

        assertEquals(1, permissionManager.listPermissions(entity, "load").size());
    }

    @Test
    @Configuration(exclude = FileStoreConfigurationTester.class)
    public void testGrantAllowedOperation() {
        User bob = createUser("bob");
        EntityManager entityManager = getEntityManager();
        AllowedOperationTypeEntity entity = new AllowedOperationTypeEntity();

        entityManager.persist(entity);

        PermissionManager permissionManager = getPermissionManager();

        permissionManager.grantPermission(bob, entity, "create");

        assertEquals(1, permissionManager.listPermissions(entity, "create").size());

        permissionManager.grantPermission(bob, entity, "delete");

        List<Permission> permissions = permissionManager.listPermissions(entity, "delete");

        List<String> operations = new ArrayList<String>();

        for (Permission permission : permissions) {
            operations.add(permission.getOperation());
        }

        assertEquals(1, permissions.size());
        assertTrue(operations.contains("delete"));

        permissions = permissionManager.listPermissions(entity, "delete, create");
        operations = new ArrayList<String>();

        for (Permission permission : permissions) {
            operations.add(permission.getOperation());
        }

        assertEquals(2, permissions.size());
        assertTrue(operations.contains("delete"));
        assertTrue(operations.contains("create"));

        permissionManager.grantPermission(bob, entity, "update, delete, create");

        permissions = permissionManager.listPermissions(entity);
        operations = new ArrayList<String>();

        for (Permission permission : permissions) {
            operations.add(permission.getOperation());
        }

        assertEquals(3, permissions.size());
        assertTrue(operations.contains("delete"));
        assertTrue(operations.contains("create"));
        assertTrue(operations.contains("update"));

        permissionManager.revokePermission(bob, entity, "delete");

        permissions = permissionManager.listPermissions(entity);
        operations = new ArrayList<String>();

        for (Permission permission : permissions) {
            operations.add(permission.getOperation());
        }

        assertEquals(2, permissions.size());
        assertTrue(operations.contains("create"));
        assertTrue(operations.contains("update"));

        permissionManager.revokePermission(bob, entity, "update,create");

        permissions = permissionManager.listPermissions(entity);

        assertTrue(permissions.isEmpty());
    }

    @Test
    @Configuration(exclude = FileStoreConfigurationTester.class)
    public void testRevokeAllowedOperation() {
        User bob = createUser("bob");
        EntityManager entityManager = getEntityManager();
        AllowedOperationTypeEntity entity = new AllowedOperationTypeEntity();

        entityManager.persist(entity);

        PermissionManager permissionManager = getPermissionManager();

        permissionManager.grantPermission(bob, entity, "create");

        assertEquals(1, permissionManager.listPermissions(entity, "create").size());

        permissionManager.grantPermission(bob, entity, "delete");

        List<Permission> permissions = permissionManager.listPermissions(entity, "delete");

        List<String> operations = new ArrayList<String>();

        for (Permission permission : permissions) {
            operations.add(permission.getOperation());
        }

        assertEquals(1, permissions.size());
        assertTrue(operations.contains("delete"));

        permissions = permissionManager.listPermissions(entity, "delete, create");
        operations = new ArrayList<String>();

        for (Permission permission : permissions) {
            operations.add(permission.getOperation());
        }

        assertEquals(2, permissions.size());
        assertTrue(operations.contains("delete"));
        assertTrue(operations.contains("create"));

        permissionManager.grantPermission(bob, entity, "update, delete, create");

        permissions = permissionManager.listPermissions(entity);
        operations = new ArrayList<String>();

        for (Permission permission : permissions) {
            operations.add(permission.getOperation());
        }

        assertEquals(3, permissions.size());
        assertTrue(operations.contains("delete"));
        assertTrue(operations.contains("create"));
        assertTrue(operations.contains("update"));

        permissionManager.revokePermission(bob, entity, "delete");

        permissions = permissionManager.listPermissions(entity);
        operations = new ArrayList<String>();

        for (Permission permission : permissions) {
            operations.add(permission.getOperation());
        }

        assertEquals(2, permissions.size());
        assertTrue(operations.contains("create"));
        assertTrue(operations.contains("update"));

        permissionManager.revokePermission(bob, entity, "create,update");

        permissions = permissionManager.listPermissions(entity);

        assertTrue(permissions.isEmpty());
    }

    @Test (expected = IdentityManagementException.class)
    @Configuration(exclude = FileStoreConfigurationTester.class)
    public void testGrantInvalidAllowedOperation() {
        User bob = createUser("bob");
        EntityManager entityManager = getEntityManager();
        AllowedOperationTypeEntity entity = new AllowedOperationTypeEntity();

        entityManager.persist(entity);

        PermissionManager permissionManager = getPermissionManager();

        permissionManager.grantPermission(bob, entity, "insert");
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
