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

import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.basic.Agent;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;

/**
 * Storage for User, Group and Role instances to enable quick resolution of identity memberships.
 *
 * TODO: dicuss if we can have only two methods here. put(IdentityType) and lookup/get(IdentityType).
 * TODO: dicuss if we need cache capabilities when using stores like the the JPA that usually have its own cache mechanisms (eg.: hibernate second level cache).
 *
 * @author Shane Bryzak
 */
public interface IdentityCache {
    /**
     * Returns the cached User object for the specified id, in the specified Realm.  If the User has
     * not previously been cached, returns null.
     *
     * @param realm
     * @param loginName
     * @return
     */
    User lookupUser(Realm realm, String loginName);

    /**
     * Returns the cached Group object with the specified group id, in the specified partition.  If the
     * Group has not previously been cached, returns null.
     *
     * @param partition
     * @param groupPath
     * @return
     */
    Group lookupGroup(Partition partition, String groupPath);

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
     * @param loginName
     * @return
     */
    Agent lookupAgent(Realm realm, String loginName);

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
    void invalidate(Partition partition, IdentityType identity);
}
