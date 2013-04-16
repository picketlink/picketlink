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

import javax.inject.Inject;

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.User;
import org.picketlink.scim.DataProvider;
import org.picketlink.scim.model.v11.SCIMGroups;
import org.picketlink.scim.model.v11.SCIMResource;
import org.picketlink.scim.model.v11.SCIMUser;

/**
 * An IDM implementation of the {@link DataProvider}
 *
 * @author anil saldhana
 * @since Apr 10, 2013
 */
public class PicketLinkIDMDataProvider implements DataProvider {

    @Inject
    private IdentityManager identityManager;

    @Override
    public SCIMUser getUser(String id) {
        SCIMUser scimUser = new SCIMUser();

        User user = identityManager.getUser(id);
        scimUser.setId(id);
        scimUser.setUserName(user.getFirstName() + " " + user.getLastName());
        // TODO: populate SCIM object
        return scimUser;
    }

    @Override
    public SCIMGroups getGroups(String id) {
        SCIMGroups scimGroup = new SCIMGroups();

        Group group = identityManager.getGroup(id);
        scimGroup.setId(id);
        return scimGroup;
    }

    @Override
    public SCIMResource getResource(String id) {
        SCIMResource scimResource = new SCIMResource();
        return scimResource;
    }

    public PicketLinkIDMDataProvider setIdentityManager(IdentityManager im) {
        this.identityManager = im;
        return this;
    }

    @Override
    public void initializeConnection() {
    }

    @Override
    public void closeConnection() {
    }
}