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
package org.picketlink.scim;

import org.picketlink.scim.model.v11.SCIMGroups;
import org.picketlink.scim.model.v11.SCIMResource;
import org.picketlink.scim.model.v11.SCIMUser;

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
    String createGroup(SCIMGroups group);

    /**
     * Get {@link SCIMUser}
     *
     * @param id
     * @return
     */
    SCIMUser getUser(String id);

    /**
     * Get {@link SCIMGroups}
     *
     * @param id
     * @return
     */
    SCIMGroups getGroups(String id);

    /**
     * Get {@link SCIMResource}
     *
     * @param id
     * @return
     */
    SCIMResource getResource(String id);

    /**
     * Close the connection to the provider
     */
    void closeConnection();
}