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
package org.picketlink.test.integration.authentication;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.SimpleAgent;
import org.picketlink.test.integration.ArchiveUtils;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author pedroigor
 */
public class AgentAuthenticationTestCase extends AbstractAuthenticationTestCase {

    public static final String AGENT_NAME = "Some Agent";

    @Deployment
    public static WebArchive createDeployment() {
        return ArchiveUtils.create(AgentAuthenticationTestCase.class);
    }

    @Override
    @Before
    public void onSetup() {
        Agent agent = this.identityManager.getAgent(AGENT_NAME);

        if (agent == null) {
            agent = new SimpleAgent(AGENT_NAME);
            this.identityManager.add(agent);
        }

        agent.setEnabled(true);

        this.identityManager.update(agent);

        Password password = new Password(USER_PASSWORD);

        this.identityManager.updateCredential(agent, password);
    }

    @Test
    public void testSuccessfulAuthentication() {
        super.credentials.setUserId(AGENT_NAME);
        super.credentials.setPassword(USER_PASSWORD);

        super.identity.login();

        assertTrue(super.identity.isLoggedIn());
        assertNotNull(super.identity.getAgent());
        assertEquals(AGENT_NAME, super.identity.getAgent().getLoginName());
    }

    @Test
    public void testUnSuccessfulAuthentication() {
        super.credentials.setUserId(AGENT_NAME);
        super.credentials.setPassword("bad_passwd");

        super.identity.login();

        assertFalse(super.identity.isLoggedIn());
        assertNull(super.identity.getAgent());
    }
}
