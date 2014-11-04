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
package org.picketlink.scim.providers;

import org.picketlink.scim.model.v11.resource.SCIMGroup;
import org.picketlink.scim.model.v11.schema.SCIMResourceType;
import org.picketlink.scim.model.v11.resource.SCIMUser;

import javax.inject.Named;

/**
 * Interface for implementation that provide the data for the SCIM endpoints
 *
 * @author anil saldhana
 * @since Apr 10, 2013
 */
@Named
public interface DataProvider {
    /**
     * Initialize the Connection to the provider
     */
    void initializeConnection();

    /**
     * Create an user
     *
     * @param user
     * @return id
     */
    String createUser(SCIMUser user);

    /**
     * Create Group
     *
     * @param group
     * @return id
     */
    String createGroup(SCIMGroup group);

    /**
     * Get {@link SCIMUser}
     *
     * @param id
     * @return
     */
    SCIMUser getUser(String id);

    /**
     * Delete an user
     * @param id
     * @return true if deletion successful
     */
    boolean deleteUser(String id);

    /**
     * Delete group
     * @param id
     * @return
     */
    boolean deleteGroup(String id);

    /**
     * Get {@link org.picketlink.scim.model.v11.resource.SCIMGroup}
     *
     * @param id
     * @return
     */
    SCIMGroup getGroups(String id);

    /**
     * Get {@link org.picketlink.scim.model.v11.schema.SCIMResourceType}
     *
     * @param id
     * @return
     */
    SCIMResourceType getResource(String id);

    /**
     * Close the connection to the provider
     */
    void closeConnection();
}