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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Membership;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.GroupQuery;
import org.picketlink.idm.query.MembershipQuery;
import org.picketlink.idm.spi.IdentityStore;

/**
 * <p>
 * Tests the query support for {@link Membership} instances.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class JPAMembershipQueryTestCase extends AbstractJPAIdentityManagerTestCase {

    private static final String USER_NAME = "theuser";
    private static final String GROUP_NAME = "Administrators";
    private static final String ROLE_NAME = "admin";
    private Group group;
    private User user;
    private Role role;
    private Membership membership;

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.test.idm.internal.jpa.AbstractJPAIdentityStoreTestCase#onSetupTest()
     */
    @Override
    @Before
    public void onSetupTest() throws Exception {
        super.onSetupTest();
        loadMemberships();
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
        MembershipQuery query = createMembershipQuery();

        query.setGroup(this.group);

        assertQueryResult(query);
        
        query.setGroup("67676");
        
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
        MembershipQuery query = createMembershipQuery();

        query.setRole(this.role);

        assertQueryResult(query);
        
        query.setRole("12121");
        
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
        MembershipQuery query = createMembershipQuery();

        query.setUser(this.user);

        assertQueryResult(query);
        
        query.setUser("12121");
        
        assertTrue(query.executeQuery().isEmpty());
    }

    /**
     * <p>
     * Create and persist a {@link Group} instance for testing.
     * </p>
     */
    private void loadMemberships() {
        IdentityStore identityStore = createIdentityStore();

        this.group = identityStore.getGroup(GROUP_NAME);
        this.role = identityStore.getRole(ROLE_NAME);
        this.user = identityStore.getUser(USER_NAME);
        this.membership = identityStore.getMembership(this.role, this.user, this.group);

        // if memberships are already loaded then do nothing
        if (this.membership != null) {
            return;
        }

        this.group = identityStore.createGroup(GROUP_NAME, null);
        this.user = identityStore.createUser(USER_NAME);
        this.role = identityStore.createRole(ROLE_NAME);

        this.membership = identityStore.createMembership(this.role, this.user, this.group);
    }

    /**
     * <p>
     * Asserts if the result returned by the specified {@link GroupQuery} match the expected values.
     * </p>
     *
     * @param query
     */
    private void assertQueryResult(MembershipQuery query) {
        IdentityStore identityStore = createIdentityStore();

        List<Membership> result = identityStore.executeQuery(query, null);

        assertFalse(result.isEmpty());
        assertEquals(this.role.getName(), result.get(0).getRole().getName());
        assertEquals(this.group.getId(), result.get(0).getGroup().getId());
        assertEquals(this.user.getId(), result.get(0).getUser().getId());
    }

    private MembershipQuery createMembershipQuery() {
        return getIdentityManager().createMembershipQuery();
    }

}