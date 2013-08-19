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
package org.picketlink.test.idm.config;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;
import org.picketlink.test.AbstractJPADeploymentTestCase;

import javax.inject.Inject;

import static org.junit.Assert.*;
import static org.picketlink.idm.model.basic.BasicModel.*;

/**
 * @author pedroigor
 */
public class SingletonStartupEJBTestCase extends AbstractJPADeploymentTestCase {

    @Inject
    private IdentityManager identityManager;

    @Inject
    private RelationshipManager relationshipManager;

    @Deployment
    public static WebArchive deploy() {
        return deploy(SingletonStartupEJBTestCase.class, IDMInitializer.class);
    }

    @Test
    public void testConfiguration() throws Exception {
        User john = new User("manuela");

        this.identityManager.add(john);

        Role tester = new Role("Manager");

        this.identityManager.add(tester);

        Group qaGroup = new Group("Human Resources");

        this.identityManager.add(qaGroup);

        grantRole(relationshipManager, john, tester);
        addToGroup(relationshipManager, john, qaGroup);
        grantGroupRole(relationshipManager, john, tester, qaGroup);

        assertTrue(hasRole(relationshipManager, john, tester));
        assertTrue(isMember(relationshipManager, john, qaGroup));
        assertTrue(hasGroupRole(relationshipManager, john, tester, qaGroup));
    }

}
