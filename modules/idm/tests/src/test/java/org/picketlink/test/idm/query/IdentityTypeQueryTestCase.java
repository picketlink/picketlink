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

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.basic.Agent;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.Tier;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.test.idm.AbstractPartitionManagerTestCase;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.testers.FileStoreConfigurationTester;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.JPAStoreConfigurationTester;
import org.picketlink.test.idm.testers.LDAPUserGroupJPARoleConfigurationTester;
import org.picketlink.test.idm.testers.SingleConfigLDAPJPAStoreConfigurationTester;

import java.util.List;

import static junit.framework.Assert.*;

/**
 * <p>
 * Test case for the Query API when retrieving {@link IdentityType} instances.
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
@Configuration(include= {JPAStoreConfigurationTester.class, FileStoreConfigurationTester.class})
public class IdentityTypeQueryTestCase extends AbstractPartitionManagerTestCase {

    public IdentityTypeQueryTestCase(IdentityConfigurationTester builder) {
        super(builder);
    }

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

        query.setParameter(IdentityType.PARTITION, getPartitionManager().getPartition(Realm.class, Realm.DEFAULT_REALM));

        List<IdentityType> result = query.getResultList();

        assertEquals(4, result.size());

        assertTrue(contains(result, agent.getId()));
        assertTrue(contains(result, user.getId()));
        assertTrue(contains(result, role.getId()));
        assertTrue(contains(result, group.getId()));

        getPartitionManager().add(new Realm("Testing"));

        Realm testingRealm = getPartitionManager().getPartition(Realm.class, "Testing");

        IdentityManager testing = getPartitionManager().createIdentityManager(testingRealm);

        agent = new Agent("Another Agent");

        testing.add(agent);

        user = new User("Another User");

        testing.add(user);

        role = new Role("Another Role");

        testing.add(role);

        group = new Group("Another Group");

        testing.add(group);

        query = testing.createIdentityQuery(IdentityType.class);

        query.setParameter(IdentityType.PARTITION, testingRealm);

        result = query.getResultList();

        assertEquals(4, result.size());

        assertTrue(contains(result, agent.getId()));
        assertTrue(contains(result, user.getId()));
        assertTrue(contains(result, role.getId()));
        assertTrue(contains(result, group.getId()));
    }

    @Test
    public void testFindByDifferentTiers() {
        getPartitionManager().add(new Tier("Application A"));

        Tier applicationATier = getPartitionManager().getPartition(Tier.class, "Application A");

        IdentityManager applicationA = getPartitionManager().createIdentityManager(applicationATier);

        Role role = new Role("Role");

        applicationA.add(role);

        Group group = new Group("Group");

        applicationA.add(group);

        IdentityQuery<IdentityType> query = applicationA.createIdentityQuery(IdentityType.class);

        query.setParameter(IdentityType.PARTITION, applicationATier);

        List<IdentityType> result = query.getResultList();

        assertEquals(2, result.size());

        assertTrue(contains(result, role.getId()));
        assertTrue(contains(result, group.getId()));

        getPartitionManager().add(new Tier("Application B"));

        Tier applicationB = getPartitionManager().getPartition(Tier.class, "Application B");
        IdentityManager identityManagerApplicationB = getPartitionManager().createIdentityManager(applicationB);

        role = new Role("Another Role");

        identityManagerApplicationB.add(role);

        group = new Group("Another Group");

        identityManagerApplicationB.add(group);

        query = identityManagerApplicationB.createIdentityQuery(IdentityType.class);

        query.setParameter(IdentityType.PARTITION, applicationB);

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