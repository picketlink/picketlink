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

package org.picketlink.test.idm.query;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.GroupRole;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.IdentityQuery;

/**
 * <p>
 * Test case for the Query API when retrieving {@link User} instances.
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class UserQueryTestCase extends AgentQueryTestCase<User> {

    @Override
    protected User createIdentityType(String name, Partition partition) {
        if (name == null) {
            name = "someUser";
        }
        
        return createUser(name, partition);
    }

    @Override
    protected User getIdentityType() {
        return getIdentityManager().getUser("someUser");
    }
    
    /**
     * <p>
     * Find an {@link User} by first name.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindByFirstNameAndLastName() throws Exception {
        User admin = createUser("admin");

        admin.setFirstName("The");
        admin.setLastName("Administrator");

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(admin);

        assertNotNull(admin);

        IdentityQuery<User> query = identityManager.<User> createIdentityQuery(User.class);

        query.setParameter(User.FIRST_NAME, "The");

        // find only by the first name
        List<User> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, admin.getId()));

        query = identityManager.<User> createIdentityQuery(User.class);

        query.setParameter(User.LAST_NAME, "Administrator");

        // find only by the last name
        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(result.size() == 1);

        query = identityManager.<User> createIdentityQuery(User.class);

        query.setParameter(User.FIRST_NAME, "The");
        query.setParameter(User.LAST_NAME, "Administrator");

        // find by first and last names
        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(result.size() == 1);

        assertEquals(admin.getId(), result.get(0).getId());

        query = identityManager.<User> createIdentityQuery(User.class);

        query.setParameter(User.FIRST_NAME, "The");
        query.setParameter(User.LAST_NAME, "Bad Administrator");

        // must not return any result because we provided a invalid last name
        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    /**
     * <p>
     * Find an {@link User} by email.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindByEmail() throws Exception {
        User admin = createUser("admin");

        admin.setEmail("admin@jboss.org");

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(admin);

        IdentityQuery<User> query = identityManager.<User> createIdentityQuery(User.class);

        query.setParameter(User.EMAIL, "admin@jboss.org");

        List<User> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(result.size() == 1);

        assertEquals(admin.getId(), result.get(0).getId());

        query = identityManager.<User> createIdentityQuery(User.class);

        query.setParameter(User.EMAIL, "badadmin@jboss.org");

        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

}
