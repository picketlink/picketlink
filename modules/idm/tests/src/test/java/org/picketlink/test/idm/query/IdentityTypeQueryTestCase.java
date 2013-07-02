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
import org.picketlink.idm.model.sample.Agent;
import org.picketlink.idm.model.sample.Group;
import org.picketlink.idm.model.sample.Realm;
import org.picketlink.idm.model.sample.Role;
import org.picketlink.idm.model.sample.Tier;
import org.picketlink.idm.model.sample.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.test.idm.AbstractIdentityManagerTestCase;
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

        Agent agent = new Agent("Agent");

        identityManager.add(agent);

        User user = new User("User");

        identityManager.add(user);

        Role role = new Role("Role");

        identityManager.add(role);

        Group group = new Group("Group");

        identityManager.add(group);

        IdentityQuery<IdentityType> query = identityManager.createIdentityQuery(IdentityType.class);

        query.setParameter(IdentityType.PARTITION, Realm.DEFAULT_REALM);

        List<IdentityType> result = query.getResultList();

        assertEquals(4, result.size());

        assertTrue(contains(result, agent.getId()));
        assertTrue(contains(result, user.getId()));
        assertTrue(contains(result, role.getId()));
        assertTrue(contains(result, group.getId()));

        IdentityManager testingRealm = getPartitionManager().createIdentityManager(getPartitionManager().getPartition(Realm.class, "Testing"));

        agent = new Agent("Another Agent");

        testingRealm.add(agent);

        user = new User("Another User");

        testingRealm.add(user);

        role = new Role("Another Role");

        testingRealm.add(role);

        group = new Group("Another Group");

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
        IdentityManager identityManager = getPartitionManager().createIdentityManager(getPartitionManager().getPartition(Tier.class, "Application A"));

        Role role = new Role("Role");

        identityManager.add(role);

        Group group = new Group("Group");

        identityManager.add(group);

        IdentityQuery<IdentityType> query = identityManager.createIdentityQuery(IdentityType.class);

        query.setParameter(IdentityType.PARTITION, "Application A");

        List<IdentityType> result = query.getResultList();

        assertEquals(2, result.size());

        assertTrue(contains(result, role.getId()));
        assertTrue(contains(result, group.getId()));

        IdentityManager testingRealm = getPartitionManager().createIdentityManager(getPartitionManager().getPartition(Tier.class, "Application B"));

        role = new Role("Another Role");

        testingRealm.add(role);

        group = new Group("Another Group");

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