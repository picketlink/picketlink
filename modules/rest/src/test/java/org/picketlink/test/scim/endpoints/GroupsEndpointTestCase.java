/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.picketlink.test.scim.endpoints;

import org.junit.Test;
import org.picketlink.scim.client.SCIMClient;
import org.picketlink.scim.endpoints.UsersEndpoint;
import org.picketlink.scim.model.v11.SCIMGroups;

import static org.junit.Assert.*;

/**
 * Unit test the {@link UsersEndpoint}
 *
 * @author anil saldhana
 * @since Apr 17, 2013
 */
public class GroupsEndpointTestCase extends AbstractEndpointTestCase {

    @Test
    public void testGet() throws Exception {
        assertTrue(server.isRunning());
        SCIMClient client = new SCIMClient();
        client.setBaseURL("http://localhost:11080/scim");

        SCIMGroups group = client.getGroup("jboss");
        assertNotNull(group);
        assertEquals("jboss", group.getId());
    }

    @Test
    public void testCreate() throws Exception {
        assertTrue(server.isRunning());
        SCIMClient client = new SCIMClient();
        client.setBaseURL("http://localhost:11080/scim");

        SCIMGroups scimGroup = new SCIMGroups();
        scimGroup.setDisplayName("samurai");

        SCIMGroups group = client.createGroup(scimGroup);
        assertEquals("samurai", group.getDisplayName());
        assertTrue(group.getId().length() > 0);
    }
}