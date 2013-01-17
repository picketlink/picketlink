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

package org.picketlink.test.idm.basic;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.test.idm.AbstractIdentityTypeTestCase;

/**
 * <p>
 * Test case for {@link Group} basic management operations.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class GroupManagementTestCase extends AbstractIdentityTypeTestCase<Group> {

    /**
     * <p>
     * Creates a new {@link Group} instance using the API. This method also checks if the group was properly created by
     * retrieving his information from the store.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testCreate() throws Exception {
        Group newGroupInstance = createGroup("someGroup", null);

        IdentityManager identityManager = getIdentityManager();

        Group storedGroupInstance = identityManager.getGroup(newGroupInstance.getName());

        assertNotNull(storedGroupInstance);
        assertEquals(newGroupInstance.getName(), storedGroupInstance.getName());
    }

    /**
     * <p>
     * Creates a new {@link Group} instance as a child of another {@link Group} using the API. This method also checks if the
     * group was properly created by retrieving his information from the store.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testCreateWithParentGroup() throws Exception {
        Group childGroup = createGroup("childGroup", "parentGroup");

        // let's retrieve the group information and see if it was properly stored
        IdentityManager identityManager = getIdentityManager();

        Group storedChildGroup = identityManager.getGroup(childGroup.getName());

        assertNotNull(storedChildGroup);
        assertNotNull(storedChildGroup.getParentGroup());
        assertEquals(childGroup.getName(), storedChildGroup.getName());
        assertEquals(childGroup.getParentGroup().getName(), storedChildGroup.getParentGroup().getName());
    }

    /**
     * <p>
     * Loads from the LDAP tree an already stored group.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testGet() throws Exception {
        Group storedGroupInstance = createIdentityType();

        IdentityManager identityManager = getIdentityManager();

        storedGroupInstance = identityManager.getGroup(storedGroupInstance.getName());

        assertNotNull(storedGroupInstance);
        assertNotNull(storedGroupInstance.getParentGroup());
        assertEquals("Test Group", storedGroupInstance.getName());
    }

    /**
     * <p>
     * Loads from the LDAP tree an already stored group.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testGetWithParent() throws Exception {
        Group storedGroupInstance = createIdentityType();

        IdentityManager identityManager = getIdentityManager();

        storedGroupInstance = identityManager.getGroup("Test Group", new SimpleGroup("Test Parent Group"));

        assertNotNull(storedGroupInstance);
        assertNotNull(storedGroupInstance.getParentGroup());
        assertEquals("Test Group", storedGroupInstance.getName());

        Group invalidGroupInstance = identityManager.getGroup("Test Group", new SimpleGroup("Invalid Parent Group"));

        assertNull(invalidGroupInstance);
    }

    /**
     * <p>
     * Remove from the LDAP tree an already stored group.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testRemove() throws Exception {
        Group storedGroupInstance = createIdentityType();

        assertNotNull(storedGroupInstance);

        IdentityManager identityManager = getIdentityManager();

        identityManager.remove(storedGroupInstance);

        Group removedGroupInstance = identityManager.getGroup(storedGroupInstance.getName());

        assertNull(removedGroupInstance);
    }

    @Override
    protected Group createIdentityType() {
        return createGroup("Test Group", "Test Parent Group");
    }

    @Override
    protected Group getIdentityType() {
        return getGroup("Test Group");
    }

}
