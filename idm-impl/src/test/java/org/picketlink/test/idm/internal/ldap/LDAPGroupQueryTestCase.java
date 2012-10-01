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
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.GroupQuery;

/**
 * <p>
 * Tests the query support for {@link Group} instances.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class LDAPGroupQueryTestCase extends AbstractLDAPIdentityManagerTestCase {

    private static final String GROUP_PARENT_NAME = "Test Parent Group";
    private static final String USER_NAME = "jduke";
    private static final String GROUP_NAME = "Test Group";

    private Group group;
    private User user;
    private Group parentGroup;

    @Before
    public void setup() throws Exception {
        super.setup();
        this.group = getIdentityManager().getGroup(GROUP_NAME);
        this.user = getIdentityManager().getUser(USER_NAME);
        this.parentGroup = getIdentityManager().getGroup(GROUP_PARENT_NAME);
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
        GroupQuery query = createQuery();

        query.setName(this.group.getName());

        assertQueryResult(query);
        
        query.setName("Invalid");
        
        assertTrue(query.executeQuery().isEmpty());
    }

    /**
     * <p>
     * Tests a simple query using the id property.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testfindById() throws Exception {
        GroupQuery query = createQuery();

        query.setId(this.group.getId());

        assertQueryResult(query);
        
        query.setId("121");
        
        assertTrue(query.executeQuery().isEmpty());
    }

    /**
     * <p>
     * Tests a simple query using the role property.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testfindByRole() throws Exception {
        GroupQuery query = createQuery();

        query.setRole("Echo");

        assertQueryResult(query);
        
        query.setRole("TheDuke");
        
        assertTrue(query.executeQuery().isEmpty());
    }

    /**
     * <p>
     * Tests a simple query using the user property.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testfindByUser() throws Exception {
        GroupQuery query = createQuery();

        query.setId(this.group.getId());
        query.setRelatedUser(this.user);

        assertQueryResult(query);
        
        query.setRelatedUser("guest");
        
        assertTrue(query.executeQuery().isEmpty());
    }

    /**
     * <p>
     * Tests a simple query using the parent group property.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testfindByParentGroup() throws Exception {
        GroupQuery query = createQuery();

        query.setId(this.group.getId());
        query.setParentGroup(this.parentGroup);

        assertQueryResult(query);
        
        query.setParentGroup("Lonely Group");
        
        assertTrue(query.executeQuery().isEmpty());
    }

    /**
     * <p>
     * Asserts if the result returned by the specified {@link GroupQuery} match the expected values.
     * </p>
     *
     * @param query
     */
    private void assertQueryResult(GroupQuery query) {
        List<Group> result = query.executeQuery();

        assertFalse(result.isEmpty());
        assertEquals(this.group.getId(), result.get(0).getId());
    }

    private GroupQuery createQuery() {
        return getIdentityManager().createGroupQuery();
    }
}