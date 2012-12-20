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

package org.picketlink.test.idm;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.IdentityQuery;

/**
 * <p>
 * Test case for the relationship between {@link User} and {@link Group} types.
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class UserGroupsRelationshipTestCase extends AbstractIdentityManagerTestCase {

    /**
     * <p>
     * Tests adding an {@link User} as a member of a {@link Group}.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testAddUserToGroup() throws Exception {
        User someUser = loadOrCreateUser("someUser", true);
        Group someGroup = loadOrCreateGroup("someGroup", null, true);

        IdentityManager identityManager = getIdentityManager();

        identityManager.addToGroup(someUser, someGroup);

        assertTrue(identityManager.isMember(someUser, someGroup));

        Group someAnotherGroup = loadOrCreateGroup("someAnotherGroup", null, true);

        assertFalse(identityManager.isMember(someUser, someAnotherGroup));

        identityManager.addToGroup(someUser, someAnotherGroup);

        assertTrue(identityManager.isMember(someUser, someAnotherGroup));
    }

    /**
     * <p>
     * Tests removing an {@link User} from a {@link Group}.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testRemoveUserFromGroup() throws Exception {
        User someUser = loadOrCreateUser("someUser", true);
        Group someGroup = loadOrCreateGroup("someGroup", null, true);
        Group someAnotherGroup = loadOrCreateGroup("someAnotherGroup", null, true);

        IdentityManager identityManager = getIdentityManager();

        identityManager.addToGroup(someUser, someGroup);
        identityManager.addToGroup(someUser, someAnotherGroup);

        assertTrue(identityManager.isMember(someUser, someGroup));
        assertTrue(identityManager.isMember(someUser, someAnotherGroup));

        identityManager.removeFromGroup(someUser, someGroup);

        assertFalse(identityManager.isMember(someUser, someGroup));

        identityManager.removeFromGroup(someUser, someAnotherGroup);

        assertFalse(identityManager.isMember(someUser, someAnotherGroup));
    }
    
    /**
     * <p>
     * Finds all groups for a specific user.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindUserGroups() throws Exception {
        Group someGroup = loadOrCreateGroup("someGroup", null, true);
        Group someAnotherGroup = loadOrCreateGroup("someAnotherGroup", null, true);
        Group someImportantGroup = loadOrCreateGroup("someImportantGroup", null, true);
        
        User user = loadOrCreateUser("someUser", true);
        
        IdentityManager identityManager = getIdentityManager();
        
        identityManager.removeFromGroup(user, someGroup);
        identityManager.removeFromGroup(user, someAnotherGroup);
        identityManager.removeFromGroup(user, someImportantGroup);
        
        IdentityQuery<Group> query = identityManager.createQuery(Group.class);
        
        query.setParameter(Group.HAS_MEMBER, new Object[] {user});
        
        List<Group> result = query.getResultList();
        
        assertFalse(contains(result, "someGroup"));
        assertFalse(contains(result, "someAnotherGroup"));
        assertFalse(contains(result, "someImportantGroup"));
        
        identityManager.addToGroup(user, someGroup);
        
        query = identityManager.createQuery(Group.class);
        
        query.setParameter(Group.HAS_MEMBER, new Object[] {user});
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertTrue(contains(result, "someGroup"));
        assertFalse(contains(result, "someAnotherGroup"));
        assertFalse(contains(result, "someImportantGroup"));
        
        identityManager.addToGroup(user, someAnotherGroup);

        query = identityManager.createQuery(Group.class);
        
        query.setParameter(Group.HAS_MEMBER, new Object[] {user});
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertTrue(contains(result, "someGroup"));
        assertTrue(contains(result, "someAnotherGroup"));
        assertFalse(contains(result, "someImportantGroup"));

        identityManager.addToGroup(user, someImportantGroup);
        
        query = identityManager.createQuery(Group.class);
        
        query.setParameter(Group.HAS_MEMBER, new Object[] {user});
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertTrue(contains(result, "someGroup"));
        assertTrue(contains(result, "someAnotherGroup"));
        assertTrue(contains(result, "someImportantGroup"));
    }

    private boolean contains(List<Group> result, String roleId) {
        for (Group resultGroup : result) {
            if (resultGroup.getName().equals(roleId)) {
                return true;
            }
        }

        return false;
    }
}
