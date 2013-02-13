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

package org.picketlink.idm;

import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;

/**
 * Storage for User, Group and Role instances to enable quick resolution of identity memberships.
 * 
 * @author Shane Bryzak
 */
public interface IdentityCache {
    /**
     * Returns the cached User object for the specified id, in the specified Realm.  If the User has
     * not previously been cached, returns null.
     * 
     * @param realm
     * @param id
     * @return
     */
    User lookupUser(Realm realm, String id);

    /**
     * Returns the cached Group object with the specified group id, in the specified partition.  If the
     * Group has not previously been cached, returns null.
     * 
     * @param partition
     * @param groupId
     * @return
     */
    Group lookupGroup(Partition partition, String groupId);

    /**
     * Returns the cached Role object with the specified name, in the specified partition.  If the
     * Role has not previously been cached, returns null.
     *  
     * @param partition
     * @param name
     * @return
     */
    Role lookupRole(Partition partition, String name);

    /**
     * Inserts the specified user into the cache, for the specified Realm.
     * 
     * @param realm
     * @param user
     */
    void putUser(Realm realm, User user);

    /**
     * Inserts the specified group into the cache, within the specified Partition.
     * 
     * @param partition
     * @param group
     */
    void putGroup(Partition partition, Group group);

    /**
     * Inserts the specified role into the cache, within the specified Partition.
     * 
     * @param partition
     * @param role
     */
    void putRole(Partition partition, Role role);

    /**
     * Returns the cached {@link Agent} object for the specified id, in the specified Realm.  If the {@link Agent} has
     * not previously been cached, returns null.
     * 
     * @param realm
     * @param id
     * @return
     */
    Agent lookupAgent(Realm realm, String id);

    /**
     * Inserts the specified {@link Agent} into the cache, within the specified Partition.
     * 
     * @param partition
     * @param role
     */
    void putAgent(Realm realm, Agent agent);

    /**
     * 
     * @param identity
     */
    //void invalidate(IdentityType identity);
}
