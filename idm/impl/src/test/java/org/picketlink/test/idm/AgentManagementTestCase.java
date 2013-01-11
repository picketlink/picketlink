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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Attribute;

/**
 * <p>
 * Test case for {@link Agent} basic management operations.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class AgentManagementTestCase extends AbstractIdentityTypeTestCase<Agent> {

    /**
     * <p>
     * Creates a new {@link Agent} instance using the API. This method also checks if the user was properly created by retrieving
     * his information from the store.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testCreate() throws Exception {
        Agent newAgent = loadOrCreateAgent("someAgent", true);

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(newAgent);

        // let's retrieve the user information and see if they are properly stored
        Agent storedAgent = identityManager.getAgent(newAgent.getLoginName());

        assertNotNull(storedAgent);

        assertEquals(newAgent.getLoginName(), storedAgent.getLoginName());
        assertTrue(storedAgent.isEnabled());
        assertTrue(new Date().compareTo(storedAgent.getCreatedDate()) > 0);
    }

    /**
     * <p>
     * Loads from the store an already stored agent.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testGet() throws Exception {
        Agent storedAgent = getIdentityType(true);

        IdentityManager identityManager = getIdentityManager();

        storedAgent = identityManager.getAgent(storedAgent.getLoginName());

        assertNotNull(storedAgent);

        assertEquals("someAgent", storedAgent.getLoginName());
    }

    /**
     * <p>
     * Updates the stored agent information.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testUpdate() throws Exception {
        Agent storedAgent = getIdentityType(true);

        assertNotNull(storedAgent);

        assertEquals("someAgent", storedAgent.getLoginName());
        
        IdentityManager identityManager = getIdentityManager();

        storedAgent.setAttribute(new Attribute<String>("someAttribute", "1"));
        
        identityManager.update(storedAgent);

        // let's load again the user from the store and check for the updated information
        Agent updatedUser = identityManager.getAgent(storedAgent.getLoginName());

        assertNotNull(updatedUser.getAttribute("someAttribute"));
        assertEquals("1", updatedUser.getAttribute("someAttribute").getValue());
    }

    /**
     * <p>
     * Remove from the store an already stored agent.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testRemove() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        Agent someAgent = getIdentityType(true);
        Agent anotherAgent = loadOrCreateAgent("someAnotherUser", true);

        assertNotNull(someAgent);
        assertNotNull(anotherAgent);

        identityManager.remove(someAgent);

        Agent removedUserInstance = getIdentityManager().getAgent(someAgent.getLoginName());

        assertNull(removedUserInstance);
        
        anotherAgent = identityManager.getAgent(anotherAgent.getLoginName());
        
        assertNotNull(anotherAgent);
    }

    @Override
    protected void updateIdentityType(Agent identityTypeInstance) {
        getIdentityManager().update(identityTypeInstance);
    }

    @Override
    protected Agent getIdentityType(boolean alwaysCreate) {
        Agent user = loadOrCreateAgent("someAgent", alwaysCreate);

        getIdentityManager().update(user);

        return user;
    }

}
