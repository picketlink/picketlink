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
package org.picketlink.test.idm.internal.ldap;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.GroupQuery;
import org.picketlink.idm.query.RoleQuery;

/**
 * <p>
 * Tests the query support for {@link Group} instances.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class LDAPRoleQueryTestCase extends AbstractLDAPIdentityManagerTestCase {

    private static final String USER_NAME = "jduke";
    private static final String GROUP_NAME = "Test Group";
    private static final String ROLE_NAME = "Echo";
    private Group group;
    private User user;
    private Role role;

    @Before
    public void setup() throws Exception {
        super.setup();
        this.group = getIdentityManager().getGroup(GROUP_NAME);
        this.user = getIdentityManager().getUser(USER_NAME);
        this.role = getIdentityManager().getRole(ROLE_NAME);
    }

    /**
     * <p>
     * Tests a simple query using the name property.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testfindByName() throws Exception {
        RoleQuery query = createRoleQuery();

        query.setName(this.role.getName());

        assertQueryResult(query);
        
        query.setName("Invalid");
        
        assertTrue(query.executeQuery().isEmpty());
    }

    /**
     * <p>
     * Tests a simple query using the group property.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testfindByGroup() throws Exception {
        RoleQuery query = createRoleQuery();

        query.setName(this.role.getName());
        query.setGroup(this.group);

        assertQueryResult(query);
        
        query.setGroup("Invalid");
        
        assertTrue(query.executeQuery().isEmpty());
    }
    
    /**
     * <p>
     * Tests a simple query using the owner property.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testfindByOwner() throws Exception {
        RoleQuery query = createRoleQuery();

        query.setOwner(this.user);

        assertQueryResult(query);
        
        query.setOwner(getIdentityManager().getUser("guest"));
        
        assertTrue(query.executeQuery().isEmpty());
    }

    /**
     * <p>
     * Asserts if the result returned by the specified {@link GroupQuery} match the expected values.
     * </p>
     *
     * @param query
     */
    private void assertQueryResult(RoleQuery query) {
        List<Role> result = query.executeQuery();

        assertFalse(result.isEmpty());
        assertEquals(this.role.getName(), result.get(0).getName());
    }

    private RoleQuery createRoleQuery() {
        return getIdentityManager().createRoleQuery();
    }

}