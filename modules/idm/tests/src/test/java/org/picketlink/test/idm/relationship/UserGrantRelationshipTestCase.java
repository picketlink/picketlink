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

package org.picketlink.test.idm.relationship;

import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.sample.User;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;

/**
 * <p>
 * Test case for the relationship between {@link User} and {@link Role} types.
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class UserGrantRelationshipTestCase extends AbstractGrantRelationshipTestCase<User> {

    public UserGrantRelationshipTestCase(IdentityConfigurationTester builder) {
        super(builder);
    }

    @Override
    protected User createIdentityType(String name) {
        return createIdentityType(name, null);
    }
    
    @Override
    protected User createIdentityType(String name, Partition partition) {
        if (name == null) {
            name = "someUser";
        }
        
        if (partition != null) {
            return createUser(name, partition);
        } else {
            return createUser(name);
        }
    }

    @Override
    protected User getIdentityType() {
        return getIdentityManager().getUser("someUser");
    }
}
