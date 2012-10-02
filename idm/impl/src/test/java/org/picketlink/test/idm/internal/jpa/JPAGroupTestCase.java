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
package org.picketlink.test.idm.internal.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.IdentityType;

/**
 * <p>
 * Tests the creation of groups using the {@link JPAIdentityManager}.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class JPAGroupTestCase extends AbstractJPAIdentityTypeTestCase {

    private static final String GROUP_NAME = "Administrators";
    private static final String GROUP_PARENT_NAME = "Company";

    /**
     * <p>
     * Tests the creation of an {@link Group} with populating some basic attributes.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testGroupStore() throws Exception {
        IdentityManager identitymanager = getIdentityManager();

        Group parentGroup = identitymanager.createGroup(GROUP_PARENT_NAME, (Group) null);
        Group group = identitymanager.createGroup(GROUP_NAME, parentGroup);

        assertNotNull(group);
        assertNotNull(group.getKey());
        assertEquals(GROUP_NAME, group.getName());

        testAddAttributes();

        testGetGroup();

        testRemoveGroup();
    }

    /**
     * <p>
     * Tests the retrieval of an {@link Group} and the removal of attributes.
     * </p>
     *
     * @throws Exception
     */
    public void testGetGroup() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        Group group = identityManager.getGroup(GROUP_NAME);

        assertNotNull(group);
        assertNotNull(group.getParentGroup());
        assertNotNull(group.getKey());
        assertEquals(GROUP_NAME, group.getName());
        assertEquals(GROUP_PARENT_NAME, group.getParentGroup().getName());

        testRemoveAttributes();
    }

    /**
     * <p>
     * Tests the remove of an {@link Group}.
     * </p>
     *
     * @throws Exception
     */
    public void testRemoveGroup() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        Group group = identityManager.getGroup(GROUP_NAME);

        assertNotNull(group);

        identityManager.removeGroup(group);

        group = identityManager.getGroup(GROUP_NAME);

        assertNull(group);
    }

    @Override
    protected IdentityType getIdentityTypeFromDatabase(IdentityManager identityStore) {
        return identityStore.getGroup(GROUP_NAME);
    }

}