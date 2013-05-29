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
package org.picketlink.test.idm.usecases;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.test.idm.AbstractIdentityManagerTestCase;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Unit test the OAuth Application Registration
 * @author anil saldhana
 * @since Dec 18, 2012
 */
public class ApplicationRegistrationTestCase extends AbstractIdentityManagerTestCase {
    
    private static final String appName = "Test App";
    private static final String appURL = "http://someurl";
    private static final String appDesc = "This is a nice app";
    
    @Test
    public void appRegister() throws Exception{
        IdentityManager identityManager = getIdentityManager();
        
        Agent oauthApp = createAgent(appName);
        
        oauthApp.setAttribute( new Attribute<String>("appURL", appURL) );
        oauthApp.setAttribute( new Attribute<String>("appDesc", appDesc) );
        
        identityManager.update(oauthApp);
        
        //Let us query
        IdentityQuery<Agent> query = identityManager.createIdentityQuery(Agent.class);

        query.setParameter(Agent.LOGIN_NAME, appName);

        List<Agent> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(result.size() == 1);

        assertEquals(appName, result.get(0).getLoginName());
        
        // let's query using only the appUrl attribute
        query = identityManager.createIdentityQuery(Agent.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("appURL"), appURL);

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(result.size() == 1);

        assertEquals(appName, result.get(0).getLoginName());
        
        //Query with a wrong agent id
        
        query = identityManager.createIdentityQuery(Agent.class);
        query.setParameter(Agent.LOGIN_NAME, "bogus");

        result = query.getResultList();

        assertTrue(result.isEmpty());
    }
    
    @Test
    public void appUnregister() throws Exception{
        IdentityManager identityManager = getIdentityManager();
        
        Agent oauthApp = createAgent(appName);
        
        oauthApp = identityManager.getAgent(appName);
        
        assertNotNull(oauthApp);
        
        identityManager.remove(oauthApp);
        
        oauthApp = identityManager.getAgent(appName);
        
        assertNull(oauthApp);
    }
}