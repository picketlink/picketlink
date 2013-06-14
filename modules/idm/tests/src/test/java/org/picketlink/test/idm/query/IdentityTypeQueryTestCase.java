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

package org.picketlink.test.idm.query;

import java.util.List;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.SimpleAgent;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.test.idm.AbstractIdentityManagerTestCase;
import sun.management.resources.agent;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * <p>
 * Test case for the Query API when retrieving {@link IdentityType} instances.
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class IdentityTypeQueryTestCase extends AbstractIdentityManagerTestCase {

    @Test
    public void testFindByDifferentRealms() {
        IdentityManager identityManager = getIdentityManager();

        SimpleAgent agent = new SimpleAgent("Agent");

        identityManager.add(agent);

        SimpleUser user = new SimpleUser("User");

        identityManager.add(user);

        SimpleRole role = new SimpleRole("Role");

        identityManager.add(role);

        SimpleGroup group = new SimpleGroup("Group");

        identityManager.add(group);

        IdentityQuery<IdentityType> query = identityManager.createIdentityQuery(IdentityType.class);

        query.setParameter(IdentityType.PARTITION, Realm.DEFAULT_REALM);

        List<IdentityType> result = query.getResultList();

        assertEquals(4, result.size());

        assertTrue(contains(result, agent.getId()));
        assertTrue(contains(result, user.getId()));
        assertTrue(contains(result, role.getId()));
        assertTrue(contains(result, group.getId()));

        IdentityManager testingRealm = getIdentityManagerFactory().createIdentityManager(getIdentityManagerFactory().getRealm("Testing"));

        agent = new SimpleAgent("Another Agent");

        testingRealm.add(agent);

        user = new SimpleUser("Another User");

        testingRealm.add(user);

        role = new SimpleRole("Another Role");

        testingRealm.add(role);

        group = new SimpleGroup("Another Group");

        testingRealm.add(group);

        query = testingRealm.createIdentityQuery(IdentityType.class);

        query.setParameter(IdentityType.PARTITION, "Testing");

        result = query.getResultList();

        assertEquals(4, result.size());

        assertTrue(contains(result, agent.getId()));
        assertTrue(contains(result, user.getId()));
        assertTrue(contains(result, role.getId()));
        assertTrue(contains(result, group.getId()));
    }

    @Test
    public void testFindByDifferentTiers() {
        IdentityManager identityManager = getIdentityManagerFactory().createIdentityManager(getIdentityManagerFactory().getTier("Application A"));

        SimpleRole role = new SimpleRole("Role");

        identityManager.add(role);

        SimpleGroup group = new SimpleGroup("Group");

        identityManager.add(group);

        IdentityQuery<IdentityType> query = identityManager.createIdentityQuery(IdentityType.class);

        query.setParameter(IdentityType.PARTITION, "Application A");

        List<IdentityType> result = query.getResultList();

        assertEquals(2, result.size());

        assertTrue(contains(result, role.getId()));
        assertTrue(contains(result, group.getId()));

        IdentityManager testingRealm = getIdentityManagerFactory().createIdentityManager(getIdentityManagerFactory().getTier("Application B"));

        role = new SimpleRole("Another Role");

        testingRealm.add(role);

        group = new SimpleGroup("Another Group");

        testingRealm.add(group);

        query = testingRealm.createIdentityQuery(IdentityType.class);

        query.setParameter(IdentityType.PARTITION, "Application B");

        result = query.getResultList();

        assertEquals(2, result.size());

        assertTrue(contains(result, role.getId()));
        assertTrue(contains(result, group.getId()));
    }

    protected boolean contains(List<IdentityType> result, String id) {
        for (IdentityType identityType : result) {
            if (identityType.getId().equals(id)) {
                return true;
            }
        }

        return false;
    }

}